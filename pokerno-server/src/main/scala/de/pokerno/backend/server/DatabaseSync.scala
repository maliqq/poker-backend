package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging}

import de.pokerno.protocol.msg
import de.pokerno.gameplay.{Notification, Route}

// TODO database pooling
class DatabaseSync(session: Option[org.squeryl.Session]) extends Actor with ActorLogging {
  import de.pokerno.db.thrift.{State => ThriftState}
  
  org.squeryl.SessionFactory.externalTransactionManagementAdapter = Some(() => {
    session
  })
  
  val service = new de.pokerno.db.Service()
  
  def receive = {
    
    case Notification(payload, Route.One(roomId), _) =>
      payload match {
        case msg.PlayerJoin(pos, amount) =>
          service.registerSeat(roomId, pos.pos, pos.player, amount.toDouble)
          
        case msg.PlayerLeave(pos) => // TODO tell how much money left
          service.unregisterSeat(roomId, pos.pos, pos.player, 0) // FIXME amount?
        
        case _ => // ignore
      }
    
    case Room.ChangedState(id, newState) =>
      service.changeRoomState(id, ThriftState.valueOf(newState.toString().toLowerCase).get)
  }
}
