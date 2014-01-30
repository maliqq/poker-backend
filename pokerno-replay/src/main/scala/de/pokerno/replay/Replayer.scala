package de.pokerno.replay

import akka.actor.Actor
import de.pokerno.protocol.{msg, rpc}
import de.pokerno.protocol.Conversions._
import de.pokerno.poker.{Card, Deck}
import de.pokerno.gameplay.{Replay, GameplayContext, Street, Streets}
import de.pokerno.model.{Dealer, Player, Table, Stake, Variation, Game, Bet, Seat}
import akka.actor.{Actor, ActorSystem, ActorLogging, ActorRef, Props}
import de.pokerno.backend.gateway.{Http, http}

import com.typesafe.config.ConfigFactory

object Replayer {
  case class Start(scenario: Scenario)

  val config = ConfigFactory.parseString(
    """
      akka {
        loglevel = "DEBUG"
        actor {
          debug {
            //receive = on
            //unhandled = on
            //lifecycle = on
          }
        }
      }
    """)

  val actorSystemConfig = ConfigFactory.load(config)
  
  val system = ActorSystem("poker-replayer", actorSystemConfig)
  val gw = system.actorOf(Props(classOf[Http.Gateway]), "http-dispatcher")

  def startHttpServer = {
    val server = new http.Server(gw,
      http.Config(port = 8080, webSocket = Right(true))
    )
    server.start
  }

  startHttpServer

  import collection.JavaConversions._

  def start(scenario: Scenario) {
    val table = scenario.table.getOrElse(
        throw ReplayError("table not defined"))
    val variation = scenario.variation.getOrElse(
        throw ReplayError("game not defined"))
    val stake = scenario.stake.getOrElse(
        throw ReplayError("stake not defined"))

    def sleep = Thread.sleep(scenario.speed * 1000)
    
    val dealer = if (scenario.deck.isDefined) new Dealer(new Deck(scenario.deck.get))
    else new Dealer
    
    val t = new Table(table.size)
    val gameplay = new GameplayContext(t, variation, stake, dealer = dealer)

    val replay = system.actorOf(Props(classOf[Replay], gameplay), "replay")
    replay ! Replay.Subscribe(gw)

    table.seatsAsList.zipWithIndex foreach { case (seat, pos) =>
      if (!seat.isEmpty)
        replay ! rpc.JoinPlayer(pos, seat.player.get, seat.stack)
    }

    var started = false
    for (street <- scenario.streets) {
      if (!started) {
        replay ! Streets.Next
        started = true
      }
      else replay ! Streets.Next

      val actions = scenario.actions.get(street)
      for (action <- actions) {
        sleep
        replay ! action
      }
    }
  }
}

case class ReplayError(msg: String) extends Exception(msg)
