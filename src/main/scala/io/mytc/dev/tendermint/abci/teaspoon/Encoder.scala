package io.mytc.dev.tendermint.abci.teaspoon

import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.util.ByteString

object Encoder {
  def apply(): Flow[ByteString, ByteString, NotUsed] = {
    Flow[ByteString].map{ msg ⇒
      val len = ByteString(BigInt(msg.size).toByteArray)
      val lenlen = ByteString(BigInt(len.size).toByteArray)
      lenlen ++ len ++ msg
    }
  }
}
