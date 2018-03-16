package io.mytc.tendermint.abci.teaspoon

import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.util.ByteString
import com.tendermint.abci._

object PBEncoder {
  def apply(): Flow[Response, ByteString, NotUsed] = {
    Flow[Response].map{ res ⇒
      ByteString(res.toByteArray)
    }
  }
}
