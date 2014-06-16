package de.pokerno.finance

import math.{BigDecimal => Decimal}
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.util.Future
import org.apache.thrift.protocol.TBinaryProtocol

object Service {
  
  import org.apache.thrift.protocol.{TBinaryProtocol, TProtocolFactory}
  
  import java.net.InetSocketAddress
  
  import com.twitter.util.Future
  import com.twitter.finagle.builder.ServerBuilder
  import com.twitter.finagle.thrift.ThriftServerFramedCodec

  import com.twitter.finagle.{Service => FinagleService}
  
  def start(addr: InetSocketAddress) {
    val protocol = new TBinaryProtocol.Factory()
    
    val service = new thrift.Balance.FinagledService(new Service, protocol)
    
    val server = ServerBuilder().
      codec(ThriftServerFramedCodec()).
      bindTo(addr).
      name("balance-server").
      build(service)
  }

}

class Service extends thrift.Balance.FutureIface {
  //import java.util.concurrent.atomic.AtomicReference
  
  val balances = collection.mutable.Map[String, Decimal]()
  val inPlay = collection.mutable.Map[String, Decimal]()
  
  def available(player: String): Future[Double] = Future {
    val value: Decimal = balances.getOrElse(player, 0)
    value.toDouble
  }
  
  def inPlay(player: String): Future[Double] = Future {
    val value: Decimal= inPlay.getOrElse(player, 0)
    value.toDouble
  }
  
  def deposit(player: String, amount: Double): Future[Unit] = Future {
    
    balances.synchronized {
      val value: Decimal = balances.getOrElse(player, 0)
      balances.put(player, value + amount)
    }
    
    inPlay.synchronized {
      val value: Decimal = balances.getOrElse(player, 0)
      inPlay.put(player, value - amount)
    }
    
  }
  
  def withdraw(player: String, amount: Double): Future[Unit] = Future {
    
    balances.synchronized {
      val value: Decimal = balances.getOrElse(player, 0)
      balances.put(player, value - amount)
    }
    
    inPlay.synchronized {
      val value: Decimal = balances.getOrElse(player, 0)
      inPlay.put(player, value + amount)
    }
    
  }
}
