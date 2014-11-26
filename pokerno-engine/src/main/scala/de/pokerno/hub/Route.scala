package de.pokerno.hub

trait Route {
  def matches(consumer: Consumer): Boolean
}

abstract class KeyBasedRoute[T] extends Route {
  implicit def consumer2routeKey(consumer: Consumer): Option[T]
}

object Route {
  case object NoOne extends Route {
    def matches(consumer: Consumer) = false
  }
  case object All extends Route {
    def matches(consumer: Consumer) = true
  }
  case class Where(f: (Consumer) ⇒ Boolean) extends Route {
    def matches(consumer: Consumer) = f(consumer)
  }

  abstract class One[T](routeKey: T) extends KeyBasedRoute[T] {
    def matches(consumer: Consumer): Boolean = {
      val key = (consumer: Option[T])
      key.isDefined && routeKey == key.get
    }
  }

  abstract class Only[T](routeKeys: List[T]) extends KeyBasedRoute[T] {
    def matches(consumer: Consumer): Boolean = {
      val key = (consumer: Option[T])
      key.isDefined && routeKeys.contains(key.get)
    }
  }

  abstract class Except[T](routeKeys: List[T]) extends KeyBasedRoute[T] {
    def matches(consumer: Consumer): Boolean = {
      val key = (consumer: Option[T])
      !key.isDefined || !routeKeys.contains(key.get)
    }
  }
}

case class RoutedMessage[T](
  msg: Message,
  from: Route = Route.NoOne,
  to: Route = Route.All
)
