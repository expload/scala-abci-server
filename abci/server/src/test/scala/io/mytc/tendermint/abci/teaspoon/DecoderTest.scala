package io.mytc.tendermint.abci.teaspoon

import akka.util.ByteString
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Source
import akka.stream.OverflowStrategy
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import scala.concurrent.Await
import scala.concurrent.duration._

class DecoderTest() extends TestKit(ActorSystem("Decoder"))
                    with ImplicitSender
                    with WordSpecLike
                    with Matchers
                    with BeforeAndAfterAll {

  implicit private val am = ActorMaterializer()

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private def mkMessage(str: String): ByteString = {
    ByteString( Array[Byte](1, str.size.toByte) ) ++ ByteString(str)
  }

  "Teaspoon Decoder" must {

    "Decode single request correctly" in {

      val sink = Decoder().toMat(Sink.fold(ByteString())(_ ++ _))(Keep.right)
      val (ref, future) = Source.actorRef(8, OverflowStrategy.fail).toMat(sink)(Keep.both).run()

      val msg = "Hello, world!"

      ref ! mkMessage(msg)
      ref ! akka.actor.Status.Success("done")

      val result = Await.result(future, 3.seconds)
      assert(ByteString(msg) == result)

    }

    "Decode several requests correctly" in {

      val sink = Decoder().toMat(Sink.fold(ByteString())(_ ++ _))(Keep.right)
      val (ref, future) = Source.actorRef(8, OverflowStrategy.fail).toMat(sink)(Keep.both).run()

      val msg1 = "Gogol"
      val msg2 = "Mogol"
      val msg3 = "Bogol"

      ref ! mkMessage(msg1)
      ref ! mkMessage(msg2)
      ref ! mkMessage(msg3)

      ref ! akka.actor.Status.Success("done")

      val result = Await.result(future, 3.seconds)
      assert(ByteString(msg1) ++ ByteString(msg2) ++ ByteString(msg3) == result)

    }

    "Decode chunked byte stream correctly" in {

      val sink = Decoder().toMat(Sink.fold(ByteString())(_ ++ _))(Keep.right)
      val (ref, future) = Source.actorRef(8, OverflowStrategy.fail).toMat(sink)(Keep.both).run()

      val msg1 = "Gogol"
      val msg2 = "Mogol"
      val msg3 = "Bogol"

      val byteStream = mkMessage(msg1) ++ mkMessage(msg2) ++ mkMessage(msg3)

      byteStream.sliding(2, 2).foreach { chunk ⇒ ref ! chunk }
      ref ! akka.actor.Status.Success("done")

      val result = Await.result(future, 3.seconds)
      assert(ByteString(msg1) ++ ByteString(msg2) ++ ByteString(msg3) == result)

    }

  }
}
