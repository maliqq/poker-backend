package de.pokerno.finance

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
  implicit def uuidFromString(s: String): java.util.UUID = java.util.UUID.fromString(s)

  type Player = String
  
  private val _balances = collection.mutable.Map[Player, Decimal]()
  private val _inPlay = collection.mutable.Map[Player, Decimal]()
  private val _refills = collection.mutable.Map[Player, java.util.Date]()
  
  def total(player: Player): Future[Double] = Future {
    val balance: Decimal = balanceOrInitial(player)
    val inplay: Decimal = _inPlay.getOrElse(player, 0)
    (balance + inplay).toDouble
  }
  
  def available(player: Player): Future[Double] = Future {
    val value: Decimal = balanceOrInitial(player)
    value.toDouble
  }
  
  def inPlay(player: Player): Future[Double] = Future {
    val value: Decimal= _inPlay.getOrElse(player, 0)
    value.toDouble
  }
  
  def deposit(player: Player, amount: Double): Future[Unit] = Future {
    
    _balances.synchronized {
      val balance: Decimal = balanceOrInitial(player)
      _balances.put(player, balance + amount)

      val inplay: Decimal = _balances.getOrElse(player, 0)
      if (inplay > amount) {// FIXME
        _inPlay.put(player, inplay - amount)
      }
    }
    
  }
  
  def withdraw(player: Player, amount: Double): Future[Unit] = Future {
    if (amount < 0) throw new thrift.Error("player %s: asked %.2f < 0" format(player, amount))
    
    _balances.synchronized {
      var balance: Decimal = _balances.getOrElse(player, 0)
      val inplay: Decimal = _inPlay.getOrElse(player, 0)
      val total = balance + inplay
      
      if (amount > balance) {
        balance = refill(player, refillAmount - inplay)
        _balances.put(player, balance)
      }
      
      if (amount > balance) {
        throw new thrift.Error("player %s: not enough money; asked: %.2f have: %.2f" format(player, amount, balance))
      }
      
      _balances.put(player, balance - amount)

      _inPlay.put(player, inplay + amount)
    }
    
  }
  
  def advance(player: Player, amount: Double): Future[Unit] = Future {
    _inPlay.synchronized {
      val inplay: Decimal = _inPlay.getOrElse(player, 0)
      val newAmount = inplay + amount
      if (newAmount > 0) {
        _inPlay.put(player, newAmount)
      } else throw new thrift.Error("player %s: can't spend %.2f of %.2f" format(player, amount, inplay))
    }
  }
  
  def join(playerId: String, amount: Double, roomId: String): Future[Unit] = Future{
    PaymentDB.join(playerId, amount, roomId)
  }
  
  def leave(playerId: String, amount: Double, roomId: String): Future[Unit] = Future{}
  
  def register(playerId: String, tournamentId: String): Future[Unit] = Future{}
  
  def freeroll(playerId: String, tournamentId: String): Future[Unit] = Future{}
  
  def ticket(playerId: String, tournamentId: String, ticketId: String): Future[Unit] = Future{}
  
  def unregister(playerId: String, tournamentId: String): Future[Unit] = Future{}
  
  def rebuy(playerId: String, tournamentId: String): Future[Unit] = Future{}
  
  def doubleRebuy(playerId: String, tournamentId: String): Future[Unit] = Future{}
  
  def addon(playerId: String, tournamentId: String): Future[Unit] = Future{}
  
  def award(playerId: String, tournamentId: String, placeNumber: Long): Future[Unit] = Future{}
  
  def bounty(playerId: String, knockedPlayerId: String, tournamentId: String): Future[Unit] = Future{
  }
  
  val refillAmount: Decimal = 10000
  val refillEvery = 1.hour
  
  import java.time.temporal.{ChronoUnit, TemporalUnit}
  import java.time.Instant
  
  private def refill(playerId: Player, amount: Decimal = refillAmount): Decimal = {
    def refilled() = {
      //Console printf("player %s: refilled with %.2f\n", player, amount)
      _refills(playerId) = new java.util.Date()
      amount
    }
    
    val lastRefill = _refills.get(playerId)
    
    lastRefill match {
      case Some(date) =>
        val now = Instant.now()
        val deadline = now.minus(refillEvery.toHours, ChronoUnit.HOURS)
        if (date.toInstant().isBefore(deadline)) refilled()
        else {
          val diff = ChronoUnit.MINUTES.between(date.toInstant(), now)
          throw new thrift.Error("player %s: can't refill balance; last refill was %d minutes ago" format(playerId, diff))
        }
      case _ => refilled()
    }
  }
  
  private def balanceOrInitial(playerId: Player): Decimal = _balances.synchronized {
    if (!_balances.contains(playerId)) {
      val amount = refill(playerId)
      _balances.put(playerId, amount)
      amount
    } else _balances.get(playerId).get
  }
}
