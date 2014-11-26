package de.pokerno.hub

trait Exchange extends Consumer {

  val consumers = collection.mutable.ListBuffer[Consumer]()

  private def register(consumer: Consumer) {
    consumers += consumer
  }
  
  final def subscribe(consumer: Consumer) = register(consumer)

  private def unregister(consumer: Consumer) {
    consumers -= consumer
  }
  final def unsubscribe(consumer: Consumer) = unregister(consumer)

  override def consume(msg: Message) {
    consumers.map(_.consume(msg))
  }
  
  final def publish(msg: Message) = consume(msg)

}

sealed class SimpleEchange extends Exchange

trait TopicExchange extends Exchange {

  def topics: Map[String, Topic]

  // register
  def subscribe(consumer: Consumer, to: String) {
    topics.get(to).map { _.subscribe(consumer) }
  }
  def subscribeAll(consumer: Consumer) {
    subscribe(consumer)
    topics.values.map { _.subscribe(consumer) }
  }

  // unregister
  def unsubscribe(consumer: Consumer, from: String) {
    topics.get(from).map { _.unsubscribe(consumer) }
  }
  def unsubscribeAll(consumer: Consumer) {
    unsubscribe(consumer)
    topics.values.map(_.unsubscribe(consumer))
  }

  // consume
  def consume(msg: Message, to: String) {
    topics.get(to).map { _.consume(msg) }
  }
  def consumeAll(msg: Message) = {
    consume(msg)
    topics.values.map(_.consume(msg))
  }

}
