package de.pokerno.hub

trait Dispatcher[T, R] extends Exchange[T] {
  final def publish(msg: T, to: R) = dispatch(msg, to)
  
  protected def dispatch(msg: T, to: R)
}

trait RouteDispatcher[T] extends Dispatcher[T, Route] {
  override def consume(msg: T) = dispatch(msg, Route.All)
  
  protected def dispatch(msg: T, to: Route) {
    to match {
      case Route.NoOne =>
      
      case Route.All =>
        consume(msg)
      
      case m: RouteMatch[T] =>
        consumers.map { consumer =>
          if (m.matches(consumer)) {
            consumer.consume(msg)
          }
        }
    }
  }
}
