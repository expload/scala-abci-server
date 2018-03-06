package io.mytc.dev.tendermint.abci
package examples

import com.tendermint.abci._
import scala.concurrent._

object DummyApi {
  def apply(): DummyApi = {
    new DummyApi()
  }
}

class DummyApi extends Api {

  override def echo(msg: String): Future[ResponseEcho] = {
    println("got echo: " + msg)
    Future.successful(ResponseEcho(msg))
  }

  def info(req: RequestInfo): Future[ResponseInfo] = {
    println(req)
    Future.successful(ResponseInfo())
  }

  def query(req:RequestQuery): Future[ResponseQuery] = {
    println(req)
    Future.successful(ResponseQuery())
  }

  def checkTx(req: RequestCheckTx): Future[ResponseCheckTx] = {
    println(req)
    Future.successful(ResponseCheckTx())
  }

  def initChain(req: RequestInitChain): Future[ResponseInitChain] = {
    println(req)
    Future.successful(ResponseInitChain())
  }

  def beginBlock(req: RequestBeginBlock): Future[ResponseBeginBlock] = {
    println(req)
    Future.successful(ResponseBeginBlock())
  }

  def endBlock(req: RequestEndBlock): Future[ResponseEndBlock] = {
    println(req)
    Future.successful(ResponseEndBlock())
  }

  def deliverTx(req: RequestDeliverTx): Future[ResponseDeliverTx] = {
    println(req)
    Future.successful(ResponseDeliverTx())
  }

  def commit(req: RequestCommit): Future[ResponseCommit] = {
    println(req)
    Future.successful(ResponseCommit())
  }

}
