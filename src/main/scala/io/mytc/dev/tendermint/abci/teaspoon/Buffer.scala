package io.mytc.dev.tendermint.abci.teaspoon

object Buffer {
  def empty: Buffer = new Buffer
}

final class Buffer {

  import akka.util.ByteString

  private var buf = ByteString.empty

  def grow(bytes: ByteString): Unit = {
    buf ++= bytes
  }

  def extractMessage(): Option[ByteString] = {
    val (lenlen, b, m) = readMessage(buf)
    buf = b
    m
  }

  def bytes: ByteString = {
    buf
  }

  private def readMessage(buf: ByteString): (Int, ByteString, Option[ByteString]) = {
    val none = (0, buf, Option.empty[ByteString])

    // The first byte in the message represents the length of the Big Endian encoded length.
    if (buf.size < 1)
      return none

    var lenlen = buf(0) & 0xFF
    if (buf.size < 1 + lenlen)
      return none

    var i = 1
    var len = 0
    while (i <= lenlen) {
      len = (len << 8) | buf(i)
      i += 1
    }

    if (buf.size < 1 + lenlen + len)
      return none

    val (msg, b) = buf.splitAt(1 + lenlen + len)
    val (hdr, m) = msg.splitAt(1 + lenlen)

    (lenlen, b, Some(m))
  }

}
