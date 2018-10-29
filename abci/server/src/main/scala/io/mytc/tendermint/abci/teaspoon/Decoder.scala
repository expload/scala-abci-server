package io.mytc.tendermint.abci.teaspoon

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage._
import akka.util.ByteString

import scala.annotation.tailrec

object Decoder {

  def apply(): Flow[ByteString, ByteString, NotUsed] = {
    Flow.fromGraph(new Decoder())
  }

  // https://tendermint.com/docs/spec/abci/client-server.html#tsp
  sealed trait TspState

  object TspState {

    case object Dummy extends TspState

    final case class ReadLength(lengthLength: Byte, buffer: ByteString) extends TspState

    final case class ReadMessage(messageLength: Int, buffer: ByteString) extends TspState

    final case class Ready(message: ByteString, rest: ByteString) extends TspState

    def process(state: TspState, incoming: ByteString): TspState = {

      // Incoming + saved buffer lengths
      def total(bs: ByteString) = incoming.length + bs.length

      // Get or else 0 for ByteString
      def g(bs: ByteString, i: Int) =
        if (bs.length < i + 1) 0
        else bs(i) & 0xFF

      @tailrec
      def rl(bs: ByteString, acc: Int, i: Int): Int = {
        val n = bs.length
        if (i < n) rl(bs, acc | g(bs, i) << ((n - 1 - i) * 8), i + 1)
        else acc
      }

      state match {
        case Dummy =>
          process(ReadLength(incoming.head, ByteString.empty), incoming.tail)
        case ReadLength(ll, buffer) if total(buffer) < ll =>
          ReadLength(ll, buffer ++ incoming)
        case ReadLength(ll, buffer) if total(buffer) >= ll =>
          val (lb, rest) = (buffer ++ incoming).splitAt(ll.toInt)
          val l = rl(lb, 0, 0)
          process(ReadMessage(l, ByteString.empty), rest)
        case ReadMessage(l, buffer) if total(buffer) < l =>
          ReadMessage(l, buffer ++ incoming)
        case ReadMessage(l, buffer) if total(buffer) >= l =>
          val (message, rest) = (buffer ++ incoming).splitAt(l)
          Ready(message, rest)
        case Ready(msg, rest) => Ready(msg, rest ++ incoming)
      }
    }
  }

}

final class Decoder extends GraphStage[FlowShape[ByteString, ByteString]] {

  import Decoder.TspState

  val in: Inlet[ByteString] = Inlet[ByteString]("teaspoon-decoder-in")
  val out: Outlet[ByteString] = Outlet[ByteString]("teaspoon-decoder-out")
  val shape = FlowShape(in, out)

  def createLogic(attrs: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) with InHandler with OutHandler {

      setHandlers(in, out, this)
      @volatile private var state: TspState = TspState.Dummy

      def onPush(): Unit = {
        // Update the state
        state = TspState.process(state, grab(in))
      }

      def onPull(): Unit = {
        state match {
          case TspState.Ready(message, rest) =>
            push(out, message)
            state = TspState.process(TspState.Dummy, rest)
            onPull()
          case _ =>
            // No ready message in the state
            // Do nothing
        }
      }
    }
  }

}
