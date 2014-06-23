package de.pokerno.backend.server

import com.twitter.util.Future
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocolFactory}
import com.twitter.finagle.{Service => FinagleService}

object Payment {
  def buildClient(addr: java.net.SocketAddress) = {
    val protocol = new TBinaryProtocol.Factory()
    val balanceService = ClientBuilder().
                                codec(ThriftClientFramedCodec()).
                                hosts(Seq(addr)).
                                hostConnectionLimit(1).
                                build()
    val balanceClient = new de.pokerno.finance.thrift.Payment.FinagledClient(balanceService)
    balanceClient
  }
}

trait Payment {
  
  val balance = Payment.buildClient(new java.net.InetSocketAddress("localhost", 3031))
  
}
