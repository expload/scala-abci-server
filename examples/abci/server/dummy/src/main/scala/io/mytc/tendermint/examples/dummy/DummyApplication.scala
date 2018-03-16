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
      cfg = Server.Config(
        host      = "127.0.0.1",
        port      = 46658,
        usock     = "/tmp/timechain/alice/abci.sock",
        nthreads  = 4
      ),
      api = DummyApi()
    )

    server.start()

  }

}
