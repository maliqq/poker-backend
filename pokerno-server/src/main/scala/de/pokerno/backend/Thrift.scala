package de.pokerno.backend

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TProtocolFactory
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.finagle.{Service => FinagleService}
import org.apache.thrift.protocol.TProtocolFactory

object Thrift {
  
  import com.twitter.finagle.{Service => FinagleService}
  
  def serve[T1 <: FinagleService[Array[Byte], Array[Byte]], T2](
      processor: T2,
      name: String,
      addr: java.net.InetSocketAddress)(implicit manifest1: Manifest[T1], manifest2: Manifest[T2]) {
    
    val protocol = new TBinaryProtocol.Factory()
    
    val service = manifest1.runtimeClass.getConstructor(
        manifest2.runtimeClass,
        classOf[TProtocolFactory]).newInstance(processor.asInstanceOf[Object], protocol).asInstanceOf[T1]
    
    val server = ServerBuilder().
      codec(ThriftServerFramedCodec()).
      bindTo(addr).
      name(name).
      build(service)
  }
  
}