package de.pokerno.replay

import akka.actor.Actor
import de.pokerno.protocol.{msg, rpc}
import de.pokerno.protocol.Conversions._
import de.pokerno.gameplay.{Replay, Street}
import de.pokerno.model.{Player, Table, Stake, Variation, Game, Bet, Seat}
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

  val system = ActorSystem("poker-replayer", ConfigFactory.load(config))
  val es = system.actorOf(Props(classOf[Http.Dispatcher]), "http-dispatcher")

  def startHttpServer = {
    val server = new http.Server(es,
      http.Config(port = 8080, eventSource = Right(true))
    )
    server.start
  }

  startHttpServer

  import collection.JavaConversions._

  def start(scenario: Scenario) {
    if (!scenario.table.isDefined) throw ReplayError("table not defined")
    if (!scenario.variation.isDefined) throw ReplayError("game not defined")
    if (!scenario.stake.isDefined) throw ReplayError("stake not defined")

    def sleep = Thread.sleep(scenario.speed * 1000)

    val replay = system.actorOf(Props(classOf[Replay], scenario.variation.get, scenario.stake.get), "replay")
    replay ! Replay.Subscribe(es)

    scenario.table.get.seatsAsList.zipWithIndex foreach { case (seat, pos) =>
      if (!seat.isEmpty) {
        replay ! rpc.JoinPlayer(pos, seat.player.get, seat.stack)
      }
    }

    var started = false
    for (street <- scenario.streets) {
      if (!started) {
        replay ! Street.Start
        started = true
      }
      else replay ! Street.Next

      val actions = scenario.actions.get(street)
      for (action <- actions) {
        sleep
        replay ! action
      }
    }
  }
}

case class ReplayError(msg: String) extends Exception(msg)
