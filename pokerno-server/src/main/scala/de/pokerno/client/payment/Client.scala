package de.pokerno.client.payment

import com.twitter.util.Future
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocolFactory}
import com.twitter.finagle.{Service => FinagleService}

object Client {
  def buildClient(addr: java.net.SocketAddress) = {
    val protocol = new TBinaryProtocol.Factory()
    val paymentService = ClientBuilder().
                                codec(ThriftClientFramedCodec()).
                                hosts(Seq(addr)).
                                hostConnectionLimit(1).
                                build()

    new de.pokerno.payment.thrift.Payment.FinagledClient(paymentService)
  }
}
