package de.pokerno.payment

import math.{BigDecimal => Decimal}
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocolFactory}
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.finagle.{Service => FinagleService}
import com.twitter.util.Future
import concurrent.duration._

object Service {
  
  import java.net.InetSocketAddress
  
  def start(addr: InetSocketAddress) {
    val protocol = new TBinaryProtocol.Factory()
    
    val service = new thrift.Payment.FinagledService(new Service, protocol)
    
    val server = ServerBuilder().
      codec(ThriftServerFramedCodec()).
      bindTo(addr).
      name("balance-server").
      build(service)
  }

}

class Service extends thrift.Payment.FutureIface {
  //import java.util.concurrent.atomic.AtomicReference
  import model._
  implicit def uuidFromString(s: String): UUID = java.util.UUID.fromString(s)
  
  def total(player: String): Future[Double] = Future {
    val playerId: UUID = player
    val balance = Balance.getOrCreate(playerId)
    balance.amount + balance.inPlay.getOrElse(.0)
  }
  
  def available(player: String): Future[Double] = Future {
    val playerId: UUID = player
    val balance = Balance.getOrCreate(playerId)
    balance.amount
  }
  
  def inPlay(player: String): Future[Double] = Future {
    val playerId: UUID = player
    val balance = Balance.getOrCreate(playerId)
    balance.inPlay.getOrElse(.0)
  }
  
  def deposit(player: String, amount: Double): Future[Unit] = Future {
    val playerId: UUID = player
    val balance = Balance.getOrCreate(playerId)
    
//    _balances.synchronized {
//      val balance: Decimal = balanceOrInitial(player)
//      _balances.put(player, balance + amount)
//
//      val inplay: Decimal = _balances.getOrElse(player, 0)
//      if (inplay > amount) {// FIXME
//        _inPlay.put(player, inplay - amount)
//      }
//    }
    
  }
  
  def withdraw(player: String, amount: Double): Future[Unit] = Future {
    if (amount < 0) throw new thrift.Error("player %s: asked %.2f < 0" format(player, amount))
    
    val playerId: UUID = player
    val balance = Balance.getOrCreate(playerId)
    
//    _balances.synchronized {
//      var balance: Decimal = _balances.getOrElse(player, 0)
//      val inplay: Decimal = _inPlay.getOrElse(player, 0)
//      val total = balance + inplay
//      
//      if (amount > balance) {
//        balance = refill(player, refillAmount - inplay)
//        _balances.put(player, balance)
//      }
//      
//      if (amount > balance) {
//        throw new thrift.Error("player %s: not enough money; asked: %.2f have: %.2f" format(player, amount, balance))
//      }
//      
//      _balances.put(player, balance - amount)
//
//      _inPlay.put(player, inplay + amount)
//    }
    
  }
  
  def advance(player: String, amount: Double): Future[Unit] = Future {
//    _inPlay.synchronized {
//      val inplay: Decimal = _inPlay.getOrElse(player, 0)
//      val newAmount = inplay + amount
//      if (newAmount > 0) {
//        _inPlay.put(player, newAmount)
//      } else throw new thrift.Error("player %s: can't spend %.2f of %.2f" format(player, amount, inplay))
//    }
  }
  
  def join(roomId: String, playerId: String, amount: Double): Future[Unit] = Future{
    Cash.join(roomId, playerId, amount)
  }
  
  def leave(roomId: String, playerId: String, amount: Double): Future[Unit] = Future{
    Cash.leave(roomId, playerId, amount)
  }
  
  def register(tournamentId: String, playerId: String): Future[Unit] = Future{
    Tournament.register(tournamentId, playerId)
  }
  
  def unregister(tournamentId: String, playerId: String): Future[Unit] = Future{}
  
  def freeroll(tournamentId: String, playerId: String): Future[Unit] = Future{
  }
  
  def ticket(tournamentId: String, playerId: String, ticketId: String): Future[Unit] = Future{
  }
  
  def rebuy(tournamentId: String, playerId: String): Future[Unit] = Future{}
  
  def doubleRebuy(tournamentId: String, playerId: String): Future[Unit] = Future{}
  
  def addon(tournamentId: String, playerId: String): Future[Unit] = Future{}
  
  def award(tournamentId: String, playerId: String, placeNumber: Long): Future[Unit] = Future{}
  
  def bounty(tournamentId: String, playerId: String, knockedPlayerId: String): Future[Unit] = Future{
  }
  
}
