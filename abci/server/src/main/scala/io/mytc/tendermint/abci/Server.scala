package io.mytc.tendermint.abci

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Tcp}
import io.mytc.akka.stream.UnixDomainSocket
import akka.util.ByteString
import com.tendermint.abci._
import io.mytc.tendermint.abci.Server.ConnectionMethod

import scala.concurrent._

object Server {

  sealed trait ConnectionMethod

  object ConnectionMethod {
    final case class Tcp(host: String, port: Int) extends ConnectionMethod
    final case class UnixSocket(path: String) extends ConnectionMethod
  }

  case class Config(
    connectionMethod: ConnectionMethod,
    nthreads: Int  = 4
  )

  def apply(cfg: Config, api: Api)
           (implicit am: Materializer, as: ActorSystem, ec: ExecutionContext): Server = {
    new Server(cfg, api)
  }

  sealed trait ServerBinding {
    def unbind(): Future[Unit]
  }
}

class Server(val cfg: Server.Config, val api: Api)
            (implicit am: Materializer, as: ActorSystem, ec: ExecutionContext) {

  def start(): Future[Server.ServerBinding] = {
    val requestHandler = Flow[ByteString]
      .via(teaspoon.Decoder())
      .via(teaspoon.PBDecoder())
      .mapAsync(cfg.nthreads)(handleRequest)
      .log("abci")
      .via(teaspoon.PBEncoder())
      .via(teaspoon.Encoder())

    cfg.connectionMethod match {
      case ConnectionMethod.Tcp(host, port) =>
        Tcp().bindAndHandle(requestHandler, host, port).map { b =>
          new Server.ServerBinding {
            def unbind(): Future[Unit] = b.unbind()
          }
        }
      case ConnectionMethod.UnixSocket(path) =>
        val sock = new java.io.File(path)
        UnixDomainSocket().bindAndHandle(requestHandler, sock).map { b =>
          new Server.ServerBinding {
            def unbind(): Future[Unit] = b.unbind()
          }
        }
    }
  }

  private def handleRequest(request: Request): Future[Response] = {
    request.value match {
      case Request.Value.Empty ⇒ api.empty()
      case Request.Value.Flush(req) ⇒ api.flush() map Response().withFlush
      case Request.Value.Echo(req) ⇒ api.echo(req.message) map Response().withEcho
      case Request.Value.Info(req) ⇒ api.info(req) map Response().withInfo
      case Request.Value.Query(req) ⇒ api.query(req) map Response().withQuery
      case Request.Value.CheckTx(req) ⇒ api.checkTx(req) map Response().withCheckTx
      case Request.Value.InitChain(req) ⇒ api.initChain(req) map Response().withInitChain
      case Request.Value.BeginBlock(req) ⇒ api.beginBlock(req) map Response().withBeginBlock
      case Request.Value.EndBlock(req) ⇒ api.endBlock(req) map Response().withEndBlock
      case Request.Value.DeliverTx(req) ⇒ api.deliverTx(req) map Response().withDeliverTx
      case Request.Value.Commit(req) ⇒ api.commit(req) map Response().withCommit
      case _ ⇒ api.notImplemented()
    }
  }
}
