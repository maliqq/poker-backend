package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.pattern.ask

import spray.routing.{HttpService, RequestContext}
import spray.http._
import util.{Success, Failure}
import concurrent.duration._

class Api extends Actor with ActorLogging with Api.Service {
  def actorRefFactory = context
  def receive = runRoute(route)
}

object Api {
  trait Service extends HttpService { a: ActorLogging =>
    val codec = de.pokerno.protocol.Codec.Json
    
    implicit def executionContext = actorRefFactory.dispatcher

    val route = pathPrefix("rooms" / Segment) { roomId =>
      pathEnd {
        get { ctx =>
          askRoom(roomId, Room.PlayState, ctx)
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
    
    def askRoom(roomId: String, msg: Any, ctx: RequestContext) {
      actorRefFactory.actorSelection(f"../node-main/$roomId").resolveOne(1 second).onComplete {
        case Success(room) =>
          val f = room.ask(msg)(1 second)
          f.onComplete {
            case Success(resp) =>
              ctx.complete(codec.encodeAsString(resp))
            case Failure(err) =>
              log.error("Error while asking {} for room {}: {}", msg, roomId, err.getCause.getMessage)
              ctx.complete(StatusCodes.InternalServerError)
          }
        case Failure(_) =>
          log.error("Room not found: {}", roomId)
          ctx.complete(StatusCodes.NotFound)
      }
    }
  } 
}
