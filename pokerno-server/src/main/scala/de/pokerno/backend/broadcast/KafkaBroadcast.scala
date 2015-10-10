package de.pokerno.backend.broadcast

import de.pokerno.protocol.{GameEvent => Codec}
import de.pokerno.util.HostPort._
import de.pokerno.backend.Broadcast

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

  def broadcast(topic: String, msg: Broadcast.Message) = {
    val data = new KeyedMessage[String, String](topic, Codec.encodeAsString(msg))
    producer.send(data)
  }
}
