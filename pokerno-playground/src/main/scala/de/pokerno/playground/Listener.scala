package de.pokerno.playground

import de.pokerno.protocol.{msg, rpc}
import de.pokerno.protocol.Conversions._
import de.pokerno.gameplay.{Replay, Street}
import de.pokerno.model.{Player, Table, Stake, Variation, Game, Bet, Seat}
import akka.actor.{Actor, ActorSystem, ActorLogging, ActorRef, Props}

object Listener {
  case class StartInstance(variation: Variation, stake: Stake)
}

class Listener(out: ActorRef) extends Actor {
  import context._
  
  var table: Option[Table] = None
  var replay = system.deadLetters

  override def preStart = {
  }

  def receive = {
    case t: Table => table = Some(t)
    case Listener.StartInstance(variation, stake) =>
      
      replay = system.actorOf(Props(classOf[Replay], variation, stake))
      replay ! Replay.Subscribe(out)
      
      (table.get.seats: List[Seat]).zipWithIndex foreach { case (seat, pos) =>
        if (!seat.isEmpty) {
          replay ! rpc.JoinPlayer(pos, seat.player.get, seat.stack)
        }
      }
    
    case addBet: rpc.AddBet => replay ! addBet

    case v @ Street.Next => replay ! v
    case v @ Street.Start => replay ! v
      
    case x: Any =>
      Console printf("unhandled!%s\n", x)
  }

  override def postStop = {
  }
}
