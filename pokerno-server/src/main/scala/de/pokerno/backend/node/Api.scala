package de.pokerno.backend.node

import de.pokerno.backend.server.{Node, Room}

import akka.actor.{Actor, ActorRef, ActorLogging, ActorSystem}
import akka.pattern.ask

import spray.routing.{HttpService, RequestContext}
import spray.http._
import util.{Success, Failure}
import concurrent.duration._

class Api(val node: ActorRef) extends Actor with ActorLogging with Api.Service {
  def actorRefFactory = context
  def receive = runRoute(route)
}

object Api {
  final val defaultPort = 8080
  final val defaultPath = "/_api"

  trait Service extends HttpService { a: ActorLogging =>
    val codec = de.pokerno.protocol.Codec.Json
    val node: ActorRef

    implicit def executionContext = actorRefFactory.dispatcher

    val route =
    pathPrefix("node") {
      path("metrics") {
        get { ctx =>
          askNode(Node.Metrics, ctx)
        }
      }
    } ~
    pathPrefix("rooms" / Segment) { roomId =>
      pathEnd {
        get { ctx =>
          askRoom(roomId, de.pokerno.form.Room.RoomState, ctx)
        }
      }
    }
    // asks

    def askNode(msg: Any, ctx: RequestContext) {
      val f = node.ask(msg)(1 second)
      f.onComplete {
        case Success(resp) =>
          ctx.complete(codec.encodeAsString(resp))
        case Failure(err) =>
          log.error("Error while asking node: {}", err.getMessage)
          ctx.complete(StatusCodes.InternalServerError)
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
