package de.pokerno.hub

trait Dispatcher[T, E <: Exchange[T], R] {
  def exchange: E
  
  final def publish(msg: T, to: R) = dispatch(msg, to)
  
  protected def dispatch(msg: T, to: R)
}

trait RouteDispatcher[T, E <: Exchange[T]] extends Dispatcher[T, E, Route] {
  //def consume(msg: T) = dispatch(msg, Route.All)
  
  protected def dispatch(msg: T, to: Route) {
    to match {
      case Route.NoOne =>
      
      case Route.All =>
        exchange.consume(msg)
      
      case m: RouteMatch[T] =>
        exchange.consumers.foreach {consumer =>
          if (m.matches(consumer)) consumer.consume(msg)
        }
    }
  }
}
