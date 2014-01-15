package de.pokerno.playground

import de.pokerno.format.text.Lexer.{Tags => t}
import de.pokerno.model
import de.pokerno.backend.{protocol => proto}
import akka.actor.Actor

class Replayer extends Actor {
  import context._
  
  override def receive = {
    case t.Table(uuid, size) =>
      var table = new model.Table(size)
      become({
        case t.Seat(uuid, stack) =>
        case t.Stake(sb, bb, ante) =>
        case t.Game(variation, limit) =>
        case t.Button(pos) =>
        case _ => unbecome()
      })
    case t.Speed(duration) =>
    case t.Street(name) =>
      become({
        case t.Sb(player) =>
        case t.Bb(player) =>
        case t.Ante(player) =>
        case t.Raise(player, amount) =>
        case t.AllIn(player) =>
        case t.Call(player, amount) =>
        case t.Fold(player) =>
        case t.Deal(player, cards, cardsNum) =>
        case x: Any =>
          unbecome()
          self ! x
      })
    case _ =>
      
  }
}
