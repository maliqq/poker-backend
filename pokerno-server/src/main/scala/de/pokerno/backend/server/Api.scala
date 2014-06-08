package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging, ActorSystem}
import spray.routing.HttpService

class Api extends Actor with ActorLogging with Api.Service {
  def actorRefFactory = context
  def receive = runRoute(route)
}

object Api {
  trait Service extends HttpService {
    val route = pathPrefix("rooms" / Segment) { roomId =>
      pathEnd {
        get {
          complete("table state")
        }
      } ~
      path("table") {
        get {
          complete("table")
        }
      } ~
      path("play") {
        get {
          complete("play")
        }
      }
    } ~ path("rooms") {
      get {
        // index
        complete("ok")
      } ~
      post {
        // create room
        complete("ok")
      } ~
      put {
        // update room
        complete("ok")
      } ~
      delete {
        complete("OK")
      }
    }
  }
}
