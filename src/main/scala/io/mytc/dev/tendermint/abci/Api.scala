package io.mytc.dev.tendermint.abci

import scala.concurrent._
import com.tendermint.abci._

trait Api {

  def empty(): Future[Response] = {
    Future.successful(Response())
  }

  def flush(): Future[ResponseFlush] = {
    Future.successful(ResponseFlush())
  }

  def echo(msg: String): Future[ResponseEcho] = {
    Future.successful(ResponseEcho(msg))
  }

  def notImplemented(): Future[Response] = {
    Future.successful(Response().withException(ResponseException(error = "Unknown request")))
  }

  def info(req: RequestInfo): Future[ResponseInfo]
  def query(req:RequestQuery): Future[ResponseQuery]
  def checkTx(request: RequestCheckTx): Future[ResponseCheckTx]
  def initChain(request: RequestInitChain): Future[ResponseInitChain]
  def beginBlock(request: RequestBeginBlock): Future[ResponseBeginBlock]
  def endBlock(request: RequestEndBlock): Future[ResponseEndBlock]
  def deliverTx(request: RequestDeliverTx): Future[ResponseDeliverTx]
  def commit(request: RequestCommit): Future[ResponseCommit]

}
