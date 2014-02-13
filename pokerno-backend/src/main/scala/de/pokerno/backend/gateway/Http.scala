package de.pokerno.backend.gateway

import akka.actor.{ Actor, ActorRef, ActorLogging }
import io.netty.channel.Channel
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil

import de.pokerno.gameplay.{Notification, Route}
import de.pokerno.backend.Gateway
import de.pokerno.protocol.{ Message, Codec ⇒ codec }
import de.pokerno.protocol.{msg => message}

object Http {
  class Gateway(node: Option[ActorRef])
      extends Actor with ActorLogging {
    
    def this() = this(None)
    
    import concurrent.duration._
    import context._

    val channelConnections = new collection.mutable.HashMap[Channel, http.Connection]()

    override def preStart {
    }

    case class Tick(conn: http.Connection)

    def receive = {
      case http.Event.Connect(channel, conn) ⇒
        //if (!conn.room.isDefined)
        //  conn.close()
        //else
          if (!channelConnections.contains(channel)) {
            channelConnections.put(channel, conn)
            log.info("{} connected", conn)
            node.map { _ ! Gateway.Connect(conn) }
          }

      case http.Event.Disconnect(channel) ⇒
        channelConnections.remove(channel).map { conn ⇒
          log.info("{} disconnected", conn)
          node.map { _ ! Gateway.Disconnect(conn) }
        }

      case http.Event.Message(channel, data) ⇒
        channelConnections.get(channel) map { conn =>
          if (conn.player.isDefined && conn.room.isDefined) {
            try {
              val msg = codec.Json.decode[message.Inbound](data.getBytes)
              log.info("got {} from {}", msg, conn)
              node.map { _ ! Gateway.Message(conn, msg) }
            } catch {
              case err: Throwable => // TODO
                log.error("message error: {}", err.getMessage)
            }
          } else log.warning("skip {} from {}", data, conn)
        }

      case Notification(msg, to) ⇒
        val data = codec.Json.encode(msg)
        log.info("broadcasting {}", msg)
        // FIXME !!!
        to match {
          case Route.All => broadcast(data)
          case Route.One(id) =>
            channelConnections.values.foreach { conn =>
              if (conn.player.getOrElse(conn.sessionId) == id)
                conn.send(msg)
            }
        }
        
      case x ⇒
        log.warning("unhandled: {}", x)
    }

    private def broadcast(msg: Any) =
      channelConnections.foreach {
        case (channel, conn) ⇒
          conn.send(msg)
      }

    override def postStop {
    }
  }
}
