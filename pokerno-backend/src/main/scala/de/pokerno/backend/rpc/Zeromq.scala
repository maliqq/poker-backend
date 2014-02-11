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
  
  def receive = {
    case m: zmq.Message =>
      try {
        
        val msg = decode(m.frames(0).toArray[Byte])
        
        log.info("[rpc] {}", msg)
        
        node ! msg
        
      } catch {
        case err: Throwable =>
          err.printStackTrace()
      }
    case m =>
      log.info("unhandled: {}", m)
  }
  
  private def decode(msg: Array[Byte]) = codec.Protobuf.decode[rpc.Request](msg)
}
