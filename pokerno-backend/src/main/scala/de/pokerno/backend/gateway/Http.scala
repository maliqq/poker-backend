package de.pokerno.backend.gateway

import akka.actor.{ Actor, ActorRef, ActorLogging }
import io.netty.channel.Channel
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil

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
          }

      case http.Event.Disconnect(channel) ⇒
        channelConnections.remove(channel).map { conn ⇒
          log.info("{} disconnected", conn)
        }

      case http.Event.Message(channel, data) ⇒
        try {
          
          channelConnections.get(channel) map { conn =>
            if (conn.player.isDefined) {
              val msg = codec.Json.decode[message.Inbound](data.getBytes)
              log.info("got {} from {}", msg, conn)
              node.map { _ ! (conn.player.get, conn.room.get, msg) }
            }
          }

        } catch {
          case err: Throwable =>
            err.printStackTrace()
        }

      case msg: Message ⇒
        log.info("broadcasting {}", msg)
        broadcast(codec.Json.encode(msg))

      case x ⇒
        log.warning("unhandled: {}", x)
    }

    def broadcast(msg: Any) =
      channelConnections.foreach {
        case (channel, conn) ⇒
          conn.send(msg)
      }

    override def postStop {
    }
  }
}
