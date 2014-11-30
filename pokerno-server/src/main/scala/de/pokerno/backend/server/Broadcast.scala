package de.pokerno.backend.server

object Broadcast {
  import de.pokerno.protocol.{GameEvent => Codec}
  import de.pokerno.util.HostPort._

  type Message = String

  object Redis {
    implicit val defaultHost = "localhost"
    implicit val defaultPort = 6379

    def apply(s: String): Redis = Redis(s: java.net.InetSocketAddress)
    def apply(addr: java.net.InetSocketAddress) = new Redis(addr.getHostName, addr.getPort)
  }

  case class Redis(host: String, port: Int) extends Broadcast {
    import de.pokerno.util.HostPort._

    val client = new redis.clients.jedis.Jedis(host, port)
    
    def broadcast(topic: String, msg: Message) = {
      client.publish(topic, msg)
    }
  }

  case class Kafka(addrs: Seq[String]) extends Broadcast {
    import kafka.producer.Producer
    import kafka.producer.KeyedMessage
    import kafka.producer.ProducerConfig
    
    val props = new java.util.Properties
    
    props.put("metadata.broker.list", addrs.mkString(","))
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("request.required.acks", "1")
    
    val config = new ProducerConfig(props)
    val producer = new Producer[String, String](config)
    
    def broadcast(topic: String, msg: Message) = {
      val data = new KeyedMessage[String, String](topic, Codec.encodeAsString(msg))
      producer.send(data)
    } 
  }

  case class Zeromq(bind: String) extends Broadcast {
    import org.zeromq.ZMQ
    
    def this(host: String, port: Int) = this(f"tcp://$host:$port")
    def this(addr: java.net.InetSocketAddress) = this(addr.getHostName, addr.getPort)
    
    val context = ZMQ.context(1) // 1 thread 
    val socket = context.socket(ZMQ.PUB)
    socket.bind(bind)
    
    def broadcast(topic: String, msg: Message) = {
      socket.sendMore(topic)
      socket.send(msg)
    }
  }
}

abstract class Broadcast {
  def broadcast(topic: String, msg: Broadcast.Message): Unit
}

// import akka.actor.Actor
// import de.pokerno.gameplay.Notification

// class Broadcasting(id: String, endpoints: Seq[Broadcast]) extends Actor {
  
//   def receive = {
//     case Notification(msg, from, to, _) =>
//       endpoints.foreach { _.broadcast(id, msg) }
//   }
  
// }
