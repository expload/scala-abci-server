package io.mytc.tendermint.abci

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Tcp}
import io.mytc.akka.stream.UnixDomainSocket
import akka.util.ByteString
import com.tendermint.abci._

import scala.concurrent._

object Server {

  case class Config(
    host: String   = "127.0.0.1",
    port: Int      = 46658,
    usock: String  = "",
    nthreads: Int  = 4
  )

  def apply(cfg: Config, api: Api)
           (implicit am: Materializer, as: ActorSystem, ec: ExecutionContext): Server = {
    new Server(cfg, api)
  }

}

class Server(val cfg: Server.Config, val api: Api)
            (implicit am: Materializer, as: ActorSystem, ec: ExecutionContext) {

  def start(): Unit = {
    val requestHandler = Flow[ByteString]
      .via(teaspoon.Decoder())
      .via(teaspoon.PBDecoder())
      .mapAsync(cfg.nthreads)(handleRequest)
      .via(teaspoon.PBEncoder())
      .via(teaspoon.Encoder())
    if (cfg.usock.isEmpty) {
      val binding = Tcp().bind(cfg.host, cfg.port)
      binding.runForeach(_.handleWith(requestHandler))
    } else {
      val sock = new java.io.File(cfg.usock)
      UnixDomainSocket().bindAndHandle(requestHandler, sock)
    }
  }

  private def handleRequest(request: Request): Future[Response] = {
    (request.value match {
      case Request.Value.Empty            ⇒ api.empty()
      case Request.Value.Flush(req)       ⇒ api.flush()           map Response().withFlush
      case Request.Value.Echo(req)        ⇒ api.echo(req.message) map Response().withEcho
      case Request.Value.Info(req)        ⇒ api.info(req)         map Response().withInfo
      case Request.Value.Query(req)       ⇒ api.query(req)        map Response().withQuery
      case Request.Value.CheckTx(req)     ⇒ api.checkTx(req)      map Response().withCheckTx
      case Request.Value.InitChain(req)   ⇒ api.initChain(req)    map Response().withInitChain
      case Request.Value.BeginBlock(req)  ⇒ api.beginBlock(req)   map Response().withBeginBlock
      case Request.Value.EndBlock(req)    ⇒ api.endBlock(req)     map Response().withEndBlock
      case Request.Value.DeliverTx(req)   ⇒ api.deliverTx(req)    map Response().withDeliverTx
      case Request.Value.Commit(req)      ⇒ api.commit(req)       map Response().withCommit
      case _                              ⇒ api.notImplemented()
    })
  }

}
