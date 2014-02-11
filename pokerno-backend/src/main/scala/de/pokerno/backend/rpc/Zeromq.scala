package de.pokerno.backend.rpc

import akka.actor.{Actor, ActorRef, ActorLogging}
import org.zeromq.ZMQ
import de.pokerno.backend.zmq
import de.pokerno.protocol.{ rpc, Codec ⇒ codec }

object Zeromq {
  final val defaultHost = "0.0.0.0"
  final val defaultPort = 5554
  
  case class Config(
      var host: String = defaultHost,
      var port: Int = defaultPort
      ) {
    def address = f"tcp://$host:$port"
  }
}

class Zeromq(node: ActorRef) extends Actor with ActorLogging {
  import context._
  val config: Zeromq.Config = Zeromq.Config()
  
  private final val socketType = ZMQ.ROUTER
  private val socket = zmq.Extension(system).socket(
      socketType,
      zmq.Listener(self),
      zmq.Bind(config.address)
    )
  
  import proto.rpc.RequestSchema.RequestType
  import de.pokerno.util.PrintUtils._
  
  def receive = {
    case m: zmq.Message =>
      try {
        val id = m.frames(0)
        val bytes = m.frames(1).toArray[Byte]
        
//        for (frame <- m.frames) {
//          Console printf("received %d bytes\n", frame.length)
//          Console println(hexdump(frame.toArray[Byte]))
//        }
        
        val request = decode(bytes)
        log.info("[rpc] {}", request.`type`)
        val msg = request.`type` match {
          case RequestType.NODE_ACTION =>
            request.nodeAction
          case RequestType.ROOM_ACTION =>
            request.roomAction
          case RequestType.TABLE_ACTION =>
            request.tableAction
          case RequestType.DEAL_ACTION =>
            request.dealAction
          case m =>
            log.error("uknown request type: {}", m)
        }
        
        node ! msg
        
      } catch {
        case err: Throwable =>
          err.printStackTrace()
      }
    case m =>
      log.info("unhandled: {}", m)
  }
  
  private def decode(msg: Array[Byte]): rpc.Request = codec.Protobuf.decode[rpc.Request](msg)
}
