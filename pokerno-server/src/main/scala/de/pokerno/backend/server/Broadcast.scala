package de.pokerno.backend.server

object Broadcast {
  import de.pokerno.protocol.{GameEvent => Codec}
  type Message = de.pokerno.protocol.GameEvent

  case class Redis(host: String, port: Int) extends Broadcast {
    val client = new redis.clients.jedis.Jedis(host, port)
    
    def broadcast(topic: String, msg: Message) =
      client.publish(topic, Codec.encodeAsString(msg))
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
  
  case class Zeromq(host: String, port: Int) extends Broadcast {
    import org.zeromq.ZMQ
    
    val context = ZMQ.context(0) 
    val socket = context.socket(ZMQ.PUB)
    socket.bind(f"tcp://$host:$port")
    
    def broadcast(topic: String, msg: Broadcast.Message) = {
      socket.send(topic)
      socket.sendMore(Codec.encodeAsString(msg))
    }
  }
}

abstract class Broadcast {
  def broadcast(topic: String, msg: Broadcast.Message): Unit
}