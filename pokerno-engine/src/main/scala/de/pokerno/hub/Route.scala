package de.pokerno.hub

trait Route

trait RouteMatch[T] extends Route {
  def matches(consumer: Consumer[T]): Boolean
}

abstract class KeyBasedRoute[T, R] extends RouteMatch[T] {
  implicit def consumer2routeKey(consumer: Consumer[T]): Option[R]
}

object Route {
  case object NoOne extends Route {
    def matches[T](consumer: Consumer[T]) = false
  }
  case object All extends Route {
    def matches[T](consumer: Consumer[T]) = true
  }
  case class Where[T](f: (Consumer[T]) ⇒ Boolean) extends Route {
    def matches(consumer: Consumer[T]) = f(consumer)
  }

  abstract class One[T, R](routeKey: R) extends KeyBasedRoute[T, R] {
    def matches(consumer: Consumer[T]): Boolean = {
      val key = (consumer: Option[R])
      key.isDefined && routeKey == key.get
    }
  }

  abstract class Only[T, R](routeKeys: List[R]) extends KeyBasedRoute[T, R] {
    def matches(consumer: Consumer[T]): Boolean = {
      val key = (consumer: Option[R])
      key.isDefined && routeKeys.contains(key.get)
    }
  }

  abstract class Except[T, R](routeKeys: List[R]) extends KeyBasedRoute[T, R] {
    def matches(consumer: Consumer[T]): Boolean = {
      val key = (consumer: Option[R])
      !key.isDefined || !routeKeys.contains(key.get)
    }
  }
}

case class RoutedMessage[T](
  msg: T,
  from: Route = Route.NoOne,
  to: Route = Route.All
)
