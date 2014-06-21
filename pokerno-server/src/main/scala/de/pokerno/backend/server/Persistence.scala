package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging}

import de.pokerno.protocol.msg
import de.pokerno.gameplay.{Notification, Route}
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
    
    case Notification(payload, Route.One(roomId), _) =>
      payload match {
        case msg.PlayerJoin(pos, amount) =>
          service.get.registerSeat(roomId, pos.pos, pos.player, amount.toDouble)
          
        case msg.PlayerLeave(pos) => // TODO tell how much money left
          service.get.unregisterSeat(roomId, pos.pos, pos.player, 0) // FIXME amount?
        
        case _ => // ignore
      }
    
    case Room.ChangedState(id, newState) =>
      service.get.changeRoomState(id, ThriftState.valueOf(newState.toString().toLowerCase).get)

  }
}
