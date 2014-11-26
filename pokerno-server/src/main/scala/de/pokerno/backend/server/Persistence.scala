package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging}

import de.pokerno.protocol.msg
import de.pokerno.gameplay.Notification
import de.pokerno.data.pokerdb

// TODO database pooling
class Persistence(service: Option[pokerdb.thrift.PokerDB.FutureIface]) extends Actor with ActorLogging {
  import pokerdb.thrift.{State => ThriftState}
  import context._
  
  override def preStart = {
    service.foreach { _ => context.become(handleAndStore) }
  }
  
  def receive = handleNothing
  
  def handleNothing: Receive = {
    case _ =>
  }
  
  def handleAndStore: Receive = {
    case Notification(payload, roomId, _, _) =>
      payload match {
        case msg.PlayerJoin(pos, amount) =>
          getService.startSession(roomId, pos.player, pos.pos, amount.toDouble)
          
        case msg.PlayerLeave(pos) => // TODO tell how much money left
          getService.endSession(roomId, pos.player, pos.pos, 0) // FIXME amount?
        
        case _ => // ignore
      }
    
    case de.pokerno.form.Room.ChangedState(id, newState) =>
      getService.changeRoomState(id, ThriftState.valueOf(newState.toString().toLowerCase).get)
  }
  
  private def getService = service.get
}
