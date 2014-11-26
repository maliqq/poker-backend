package de.pokerno.hub

trait Exchange[T] extends Consumer[T] {

  val consumers = collection.mutable.ListBuffer[Consumer[T]]()

  private def register(consumer: Consumer[T]) {
    consumers += consumer
  }
  
  final def subscribe(consumer: Consumer[T]) = register(consumer)

  private def unregister(consumer: Consumer[T]) {
    consumers -= consumer
  }
  final def unsubscribe(consumer: Consumer[T]) = unregister(consumer)

  override def consume(msg: T) {
    consumers.map(_.consume(msg))
  }
  
  final def publish(msg: T) = consume(msg)

}

sealed class SimpleEchange extends Exchange[Message]

trait TopicExchange[T] extends Exchange[T] {

  def topics: Map[String, Topic[T]]

  // register
  def subscribe(consumer: Consumer[T], to: String) {
    topics.get(to).map { _.subscribe(consumer) }
  }
  def subscribeAll(consumer: Consumer[T]) {
    subscribe(consumer)
    topics.values.map { _.subscribe(consumer) }
  }

  // unregister
  def unsubscribe(consumer: Consumer[T], from: String) {
    topics.get(from).map { _.unsubscribe(consumer) }
  }
  def unsubscribeAll(consumer: Consumer[T]) {
    unsubscribe(consumer)
    topics.values.map(_.unsubscribe(consumer))
  }

  // consume
  def consume(msg: T, to: String) {
    topics.get(to).map { _.consume(msg) }
  }
  def consumeAll(msg: T) = {
    consume(msg)
    topics.values.map(_.consume(msg))
  }

}
