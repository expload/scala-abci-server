package io.mytc.dev.tendermint.abci.teaspoon

import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.util.ByteString
import com.google.protobuf.CodedInputStream
import com.tendermint.abci._

object PBDecoder {
  def apply(): Flow[ByteString, Request, NotUsed] = {
    Flow[ByteString].map{ bytes ⇒
      Request.defaultInstance.mergeFrom(CodedInputStream.newInstance(bytes.toByteBuffer))
    }
  }
}
