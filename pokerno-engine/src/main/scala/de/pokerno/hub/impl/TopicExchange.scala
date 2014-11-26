package de.pokerno.hub.impl

class TopicExchange[T] extends de.pokerno.hub.TopicExchange[T] {
  def exchange = new Exchange[T]()
  val topics = collection.mutable.Map[String, Topic[T]]()
}
