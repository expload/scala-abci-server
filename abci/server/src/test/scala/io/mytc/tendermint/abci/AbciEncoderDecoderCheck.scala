package io.mytc.tendermint.abci

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.util.ByteString
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Properties}

import scala.collection.immutable.Seq
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

object AbciEncoderDecoderCheck extends Properties("AbciEncoderDecoder") {

  implicit val as = ActorSystem("abci-encoder-decoder")
  implicit val am = ActorMaterializer()
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  private val genByteString =
    Gen.nonEmptyContainerOf[Array, Byte](Arbitrary.arbByte.arbitrary).map(ByteString(_: _*))

  private val genBss = Gen.nonEmptyContainerOf[Seq, ByteString](genByteString)

  property("Encoder -> Decoder") = {
    val flow = Flow[ByteString]
      .via(teaspoon.Encoder())
      .via(teaspoon.Decoder())

    val gen =
      Gen.nonEmptyContainerOf[Seq, Seq[ByteString]](genBss)

    forAll(gen) { arr =>
      val bsss = arr.map(_.map(ByteString(_: _*)))

      val (pub, sub) = TestSource.probe[ByteString]
        .via(flow)
        .toMat(TestSink.probe[ByteString])(Keep.both)
        .run()

      Try {
        bsss.foreach { bss =>
          sub.request(bss.length.toLong)
          bss.foreach(pub.sendNext)
          sub.expectNextUnorderedN(bss)
        }
      }.isSuccess
    }
  }

  property("Encoder -> Decoder") = {
    val gen = for {
      bss <- genBss
      encoded = Await.result(Source[ByteString](bss).via(teaspoon.Encoder()).runWith(Sink.seq), 3.seconds)
      merged = encoded.reduce(_ ++ _)
      numParts <- Gen.chooseNum(1, merged.length)
      parts = merged.grouped(numParts).toSeq
    } yield (bss, parts)

    forAll(gen) { case (bss, parts) =>
      val (pub, sub) = TestSource.probe[ByteString]
        .via(teaspoon.Decoder())
        .toMat(TestSink.probe[ByteString])(Keep.both)
        .run()

      Try {
        sub.request(bss.length.toLong)
        parts.foreach(pub.sendNext)
        sub.expectNextN(bss)
      }.isSuccess
    }
  }
}
