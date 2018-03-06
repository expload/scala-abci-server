package io.mytc.dev.tendermint.abci
package examples

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
