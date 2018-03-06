package io.mytc.dev.tendermint.abci.teaspoon

import akka.stream._
import akka.stream.stage._
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing.FramingException
import akka.NotUsed
import akka.util.ByteString

object Decoder {
  def apply(): Flow[ByteString, ByteString, NotUsed] = {
    Flow.fromGraph(new Decoder())
  }
}

final class Decoder extends GraphStage[FlowShape[ByteString, ByteString]] {

  private val in = Inlet[ByteString]("teaspoon-decoder-in")
  private val out = Outlet[ByteString]("teaspoon-decoder-out")
  override val shape = FlowShape(in, out)

  override def createLogic(attrs: Attributes) = new GraphStageLogic(shape) with InHandler with OutHandler {

    setHandlers(in, out, this)

    private var buf = Buffer.empty

    private def tryPushMessage(): Unit = {
      val msgOpt = buf.extractMessage()
      if (msgOpt.isEmpty) {
        tryPull()
      } else {
        msgOpt.map { msg ⇒
          push(out, msg)
        }
      }
    }

    private def tryPull(): Unit = {
      if (isClosed(in)) {
        failStage(new FramingException("Stream finished but there was a truncated final frame in the buffer"))
      } else {
        pull(in)
      }
    }

    override def onPush(): Unit = {
      val bytes = grab(in)
      buf.grow(bytes)
      tryPushMessage()
    }

    override def onPull(): Unit = {
      tryPushMessage()
    }

  }

}
