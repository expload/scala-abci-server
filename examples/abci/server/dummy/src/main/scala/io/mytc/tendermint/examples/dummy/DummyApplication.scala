package io.mytc.tendermint.abci
package examples.dummy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.mytc.tendermint.abci.Server.ConnectionMethod

object DummyApplication {

  def main(args: Array[String]): Unit = {

    implicit val as = ActorSystem("dummy-tendermint-abci")
    implicit val am = ActorMaterializer()
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val server = Server(
      cfg = Server.Config(ConnectionMethod.Tcp("127.0.0.1", 46658)),
      api = DummyApi()
    )

    server.start()

  }

}
