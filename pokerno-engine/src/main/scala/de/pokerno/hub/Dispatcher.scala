package de.pokerno.hub

trait Dispatcher[T] extends Exchange {
  final def publish(msg: Message, to: T) = dispatch(msg, to)
  
  protected def dispatch(msg: Message, to: T)
}

trait RouteDispatcher extends Dispatcher[Route] {
  override def consume(msg: Message) = dispatch(msg, Route.All)
  
  protected def dispatch(msg: Message, to: Route) {
    if (to == Route.NoOne) {
      return
    }
    if (to == Route.All) {
      consume(msg)
      return
    }
    consumers.map { consumer =>
      if (to.matches(consumer)) {
        consumer.consume(msg)
      }
    }
  }
}
