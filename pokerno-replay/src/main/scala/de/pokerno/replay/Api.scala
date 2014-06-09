package de.pokerno.replay

import akka.actor.{ Actor, ActorRef, ActorLogging }
import akka.pattern.ask

import spray.routing.{HttpService, RequestContext}
import spray.http._
import spray.http.HttpHeaders._

import com.fasterxml.jackson.databind.ObjectMapper

import util.{Success, Failure}
import concurrent.duration._

private[replay] class Api extends Actor with ActorLogging with ApiService {
  
  def actorRefFactory = context
  def receive = runRoute(route)
  
}

trait ApiService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  val route = path("_api" / "scenario") {
    respondWithHeaders(
        `Access-Control-Allow-Origin`(AllOrigins),
        `Access-Control-Allow-Headers`("*")) {
      head {
        complete(StatusCodes.OK)
      } ~
      post {
        parameters('id) { id =>
          entity(as[String]) { content => ctx =>
            submit(id, content, ctx)
          }
        }
      }
    }
  }
  
  def submit(id: String, content: String, ctx: RequestContext) {
    actorRefFactory.actorSelection("../replayer").resolveOne(1 second).onComplete {
      case Success(ref) =>
        ref ! (id, content, ctx)
      case Failure(_) =>
        ctx.complete("")
    }
  }
}
