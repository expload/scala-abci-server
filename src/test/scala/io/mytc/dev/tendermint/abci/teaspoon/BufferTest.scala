package io.mytc.dev.tendermint.abci.teaspoon

import org.scalatest._
import akka.util.ByteString

class BufferTest extends FlatSpec with Matchers {

  "A buffer" must "initially be empty" in {
    val buf = Buffer.empty
    assert(buf.bytes.size == 0)
    assert(Option.empty[ByteString] == buf.extractMessage())
  }

  it must "be mutable and be able to grow" in {
    val chunk1 = ByteString("hello")
    val chunk2 = ByteString("world")
    val all = chunk1 ++ chunk2
    val buf = Buffer.empty

    buf.grow(chunk1)
    assert(chunk1.size == buf.bytes.size)

    buf.grow(chunk2)
    assert(all == buf.bytes)
  }

  it must "extract message consistently (1)" in {
    val msg = "hello"
    val bytes = ByteString( Array[Byte](2, 0, msg.size.toByte) ) ++ ByteString(msg)
    val buf = Buffer.empty

    buf.grow(bytes)

    val msgOpt = buf.extractMessage()

    assert(!msgOpt.isEmpty)
    assert(Some(ByteString(msg)) == msgOpt)
  }

  it must "extract message consistently (2)" in {
    val msg = "hello"
    val bytes = ByteString( Array[Byte](1, msg.size.toByte) ) ++ ByteString(msg)
    val buf = Buffer.empty

    buf.grow(bytes)

    val msgOpt = buf.extractMessage()

    assert(!msgOpt.isEmpty)
    assert(buf.bytes == ByteString())
    assert(Some(ByteString(msg)) == msgOpt)
  }

  it must "extract message consistently (3)" in {
    val msg = "hello"
    val bytes = ByteString( Array[Byte](1, msg.size.toByte) ) ++ ByteString(msg)
    val buf = Buffer.empty

    buf.grow(bytes)

    val msgOpt = buf.extractMessage()

    assert(!msgOpt.isEmpty)
    assert(buf.bytes == ByteString())
    assert(Some(ByteString(msg)) == msgOpt)
  }

  it must "extract message consistently after one extraction" in {
    val msg = "hello"
    val bytes = ByteString( Array[Byte](1, msg.size.toByte) ) ++ ByteString(msg)
    val buf = Buffer.empty

    buf.grow(bytes)
    val msgOpt1 = buf.extractMessage()

    assert(!msgOpt1.isEmpty)
    assert(buf.bytes == ByteString())
    assert(Some(ByteString(msg)) == msgOpt1)

    buf.grow(bytes)
    val msgOpt2 = buf.extractMessage()

    assert(!msgOpt2.isEmpty)
    assert(buf.bytes == ByteString())
    assert(Some(ByteString(msg)) == msgOpt2)
  }

  it must "extract message consistently and leave remaining bytes unchanged" in {
    val msg = "hello"
    val bytes = ByteString( Array[Byte](1, msg.size.toByte) ) ++ ByteString(msg)
    val chunk = ByteString( Array[Byte](1, 2, 0, 3, 2, 2) )
    val buf = Buffer.empty

    buf.grow(bytes)
    buf.grow(chunk)
    buf.extractMessage()

    val rbytes = buf.bytes

    assert(chunk == rbytes)
  }

}
