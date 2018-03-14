package io.mytc.tendermint.abci.teaspoon

import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.util.ByteString
import com.google.protobuf.CodedOutputStream

object Encoder {
  def apply(): Flow[ByteString, ByteString, NotUsed] = {
    Flow[ByteString].map{ msg ⇒
      // Header is a varint encoded length with notag
      // https://developers.google.com/protocol-buffers/docs/encoding#varints
      // https://github.com/jTendermint/jabci/blob/aff15ae3dbe3e698e706c65c4a46342dd572432b/src/main/java/com/github/jtendermint/jabci/socket/TSocket.java#L221
      val tmpbuf = new Array[Byte](10 + msg.size)
      val out = CodedOutputStream.newInstance(tmpbuf)
      out.writeUInt64NoTag(CodedOutputStream.encodeZigZag64(msg.size.asInstanceOf[Long]))
      out.flush()
      ByteString(tmpbuf).splitAt(out.getTotalBytesWritten)._1 ++ msg
    }
  }
}
