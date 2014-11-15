package de.pokerno.backend.server.node

import akka.actor.ActorRef
import com.twitter.util.Future
import de.pokerno.protocol.{thrift, cmd}
import java.nio.ByteBuffer
import de.pokerno.backend.server.{Node, Room}
import de.pokerno.backend.Thrift
import de.pokerno.model._

object Service {
  def apply(node: ActorRef, addr: java.net.InetSocketAddress) = {
    Thrift.serve[thrift.rpc.Node.FinagledService, thrift.rpc.Node.FutureIface](new Service(node), "NodeService", addr)
  }
}

class Service(node: ActorRef) extends thrift.rpc.Node.FutureIface {
  import java.nio.ByteBuffer
  import de.pokerno.model.ThriftConversions._
  
  def createRoom(id: String,
      variation: thrift.Variation,
      stake: thrift.Stake,
      table: thrift.Table): Future[Unit] = Future{
    node ! Node.CreateRoom(id, variation, stake)
  }
  
  def maintenance: Future[Unit] = Future{}
  
  def close(id: String): Future[Unit] = Future{
    node ! Node.ChangeRoomState(id, de.pokerno.form.Room.Close)
  }
  
  def pause(id: String, reason: thrift.rpc.PauseReason): Future[Unit] = Future{
    node ! Node.ChangeRoomState(id, de.pokerno.form.Room.Pause)
  }
  
  def resume(id: String): Future[Unit] = Future{
    node ! Node.ChangeRoomState(id, de.pokerno.form.Room.Resume)
  }
  
  def cancelCurrentDeal(id: String) = Future{}

  import de.pokerno.model.Player
  def joinPlayer(id: String, player: String, pos: Int, amount: Double): Future[Unit] = Future{
    node ! Node.SendCommand(id, cmd.JoinPlayer(pos, player, amount))
  }
  
  def kickPlayer(id: String, player: String, reason: thrift.rpc.KickReason): Future[Unit] = Future{
    node ! Node.SendCommand(id, cmd.KickPlayer(player))
  }
  
  def dealCards(id: String, dealType: de.pokerno.protocol.thrift.DealType, cards: ByteBuffer, cardsNum: Int, player: String): Future[Unit] = Future{}
  
  def addBet(id: String, player: String, bet: de.pokerno.protocol.thrift.Bet): Future[Unit] = Future{}
  
  def discardCards(id: String, player: String, cards: ByteBuffer, standPat: Boolean): Future[Unit] = Future{}
  
  def showCards(id: String, player: String, cards: ByteBuffer, muck: Boolean): Future[Unit] = Future{}
  
  def leave(id: String, player: String): Future[Unit] = Future{}
  
  def sitOut(id: String, player: String): Future[Unit] = Future{}
  
  def comeBack(id: String, player: String): Future[Unit] = Future{}
  
  def offline(id: String, player: String): Future[Unit] = Future{}
  
  def online(id: String, player: String): Future[Unit] = Future{}
  
  def buyIn(id: String, player: String, amount: Double): Future[Unit] = Future{}
  
  def rebuy(id: String, player: String, amount: Double): Future[Unit] = Future{}
  
  def doubleRebuy(id: String, player: String, amount: Double): Future[Unit] = Future{}
  
  def addon(id: String, player: String, amount: Double): Future[Unit] = Future{}
}
