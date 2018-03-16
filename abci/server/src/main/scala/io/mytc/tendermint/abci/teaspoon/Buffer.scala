package io.mytc.tendermint.abci.teaspoon

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
    val (remaining, message) = readMessage(buf)
    buf = remaining
    message
  }

  def bytes: ByteString = {
    buf
  }

  private def readMessage(buf: ByteString): (ByteString, Option[ByteString]) = {
    import com.google.protobuf.CodedInputStream
    val none = (buf, Option.empty[ByteString])
    if (buf.size < 1)
      return none
    // Header is a varint encoded length with notag
    // https://developers.google.com/protocol-buffers/docs/encoding#varints
    // https://github.com/jTendermint/jabci/blob/aff15ae3dbe3e698e706c65c4a46342dd572432b/src/main/java/com/github/jtendermint/jabci/socket/TSocket.java#L221
    try {
      val in = CodedInputStream.newInstance(buf.iterator.asInputStream)
      val len = CodedInputStream.decodeZigZag64(in.readUInt64()).asInstanceOf[Int]
      val lenlen = in.getTotalBytesRead
      val (msg, b) = buf.splitAt(lenlen + len)
      val (hdr, m) = msg.splitAt(lenlen)
      (b, Some(m))
    } catch {
      case e: Exception ⇒
        println("decode error: " + e.getMessage)
        none
    }
  }

}
