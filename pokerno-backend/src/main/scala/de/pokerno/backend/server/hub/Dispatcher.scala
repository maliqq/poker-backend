package de.pokerno.backend.server.hub

trait Dispatcher extends Exchange {
  
  def topics: Map[String, Topic]
  
  import collection.JavaConversions._
  
  def dispatch(msg: Message) = consumers.map(_.consume(msg))
  def dispatch(topic: String, msg: Message) = topics.get(topic).map { topic =>
    topic.consumers.map(_.consume(msg))
  }
  def dispatch(consumer: Consumer, msg: Message) = consumer.consume(msg)
  
  override def unregister(consumer: Consumer) {
    super.unregister(consumer)
    topics.values.map(_.unregister(consumer))
  }
  
}
