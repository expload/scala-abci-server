package io.mytc.tendermint.abci
package examples.dummy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object DummyApplication {

  def main(args: Array[String]): Unit = {

    implicit val as = ActorSystem("dummy-tendermint-abci")
    implicit val am = ActorMaterializer()
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val server = Server(
      cfg = Server.Config(),
      api = DummyApi()
    )

    server.start()

  }

}
