package de.pokerno.replay

import de.pokerno.protocol.{msg, rpc}
import de.pokerno.protocol.Conversions._
import de.pokerno.poker.{Card, Deck}
import de.pokerno.gameplay.{Replay, Context => GameplayContext, Street, Streets}
import de.pokerno.model.{Dealer, Player, Table, Stake, Variation, Game, Bet, Seat}
import akka.actor.{Actor, ActorSystem, ActorLogging, ActorRef, Props, Kill}


object Replayer {
  case class Replay(scenario: Scenario)
}

case class ReplayError(msg: String) extends Exception(msg)

class Replayer(gw: ActorRef) extends Actor {
  import io.netty.handler.codec.http
  import io.netty.channel.{ChannelHandlerContext, ChannelFutureListener}
  import http.HttpHeaders._
  
  def receive = {
    // http request
    case (content: String, ctx: ChannelHandlerContext, resp: http.DefaultFullHttpResponse) =>
      resp.headers().add(Names.CONTENT_TYPE, "application/json")
      try {
        val src = scala.io.Source.fromString(content)
        val scenario = Scenario.parse(src)
        replay(scenario)
        resp.content().writeBytes("""{"status": "ok"}""".getBytes)
      } catch {
        case err: ReplayError =>
          resp.setStatus(http.HttpResponseStatus.UNPROCESSABLE_ENTITY)
          resp.content().writeBytes(
              """{"status": "error", "error": "%s"}""".format(err.getMessage).getBytes)
      }
      ctx.channel.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE)
    
    // console request
    case Replayer.Replay(scenario) =>
      try {
        replay(scenario)
      } catch {
        case err: ReplayError =>
          Console printf("[ERROR] %s%s%s\n", Console.RED, err.getMessage, Console.RESET)
      }
      
    case _ =>
  }
  
  import context._
  import collection.JavaConversions._
  
  def replay(scenario: Scenario) {
    val table = scenario.table.getOrElse(
        throw ReplayError("table not defined"))
    val variation = scenario.variation.getOrElse(
        throw ReplayError("game not defined"))
    val stake = scenario.stake.getOrElse(
        throw ReplayError("stake not defined"))
    
    val dealer = if (scenario.deck.isDefined) new Dealer(new Deck(scenario.deck.get))
    else new Dealer
    
    //val t = new Table(table.size)
    val gameplay = new GameplayContext(table, variation, stake, dealer = dealer)

    val replay = system.actorOf(Props(classOf[Replay], gameplay))
    replay ! Replay.Subscribe(gw)

//    table.seatsAsList.zipWithIndex foreach { case (seat, pos) =>
//      if (!seat.isEmpty)
//        replay ! rpc.JoinPlayer(pos, seat.player.get, seat.stack)
//    }

    for (streetName <- scenario.streets) {
      val street: Option[Street.Value] = streetName
      if (street.isDefined)
        replay ! Replay.StreetActions(street.get, scenario.actions.get(streetName).toList, scenario.speed)
    }
    
    replay ! Kill
  }
}
