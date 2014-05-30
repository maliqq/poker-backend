package de.pokerno.gameplay

import de.pokerno.protocol.GameEvent

class Events(id: String) {
  
  lazy val broker = new Broker(id)
  
  trait RouteBuilder {
    def all()               = Route.All
    def one(id: String)     = Route.One(id)
    def except(id: String)  = Route.Except(List(id))
    def only(id: String)    = Route.One(id)
  }
  
  object RouteBuilder extends RouteBuilder
  
  def publish(evt: GameEvent)(f: RouteBuilder => Route) {
    broker.publish(Notification(evt, from = Route.One(id), to = f(RouteBuilder)))
  }
  
}
