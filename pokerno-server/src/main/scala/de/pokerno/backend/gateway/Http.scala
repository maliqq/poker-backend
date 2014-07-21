package de.pokerno.backend.gateway

import akka.actor.{ Actor, ActorRef, ActorLogging }
import io.netty.channel.Channel
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil

import de.pokerno.gameplay.{ Notification, Route }
import de.pokerno.backend.Gateway
import de.pokerno.protocol.{GameEvent, PlayerEvent}

object Http {
  
  object Event {
    abstract class Connect
    abstract class Disconnect
    abstract class Message
  }
  
  abstract class Events {
    def connect(conn: http.Connection): Event.Connect
    def disconnect(conn: http.Connection): Event.Disconnect
    def message(conn: http.Connection, msg: String): Event.Message
  }
  
  class Gateway(ref: ActorRef, events: Events)
      extends Actor with ActorLogging {
    
    import concurrent.duration._
    import context._

    val channelConnections = new collection.mutable.HashMap[Channel, http.Connection]()

    override def preStart {
    }

    case class Tick(conn: http.Connection)

    def receive = {
      case http.ConnectionEvent.Connect(channel, conn) ⇒
        //if (!conn.room.isDefined)
        //  conn.close()
        //else
        if (!channelConnections.contains(channel)) {
          channelConnections.put(channel, conn)
          log.info("{} connected", conn)
          ref ! events.connect(conn)
          //node.map { _ ! Gateway.Connect(conn) }
        }

      case http.ConnectionEvent.Disconnect(channel) ⇒
        channelConnections.remove(channel).map { conn ⇒
          log.info("{} disconnected", conn)
          ref ! events.disconnect(conn)
          //node.map { _ ! Gateway.Disconnect(conn) }
        }

      case http.ConnectionEvent.Message(channel, data) ⇒
        channelConnections.get(channel) map { conn ⇒
          if (conn.player.isDefined && conn.room.isDefined) {
            try {
              log.info("got {} from {}", data, conn)
              ref ! events.message(conn, data)
              //node.map { _ ! Gateway.Message(conn, msg) }
            } catch {
              case err: Throwable ⇒ // TODO
                log.error("message error: {}", err.getMessage)
            }
          } else log.warning("skip {} from {}", data, conn)
        }

      // TODO: moved to watchers?
      // case Notification(msg, _, to) ⇒
      //   broadcast(msg, to)(channelConnections.values)

      case x ⇒
        log.warning("unhandled: {}", x)
    }

    override def postStop {
    }
  }
}
