package de.pokerno.playground

import de.pokerno.format.text.Lexer.{Tags => tags}
import de.pokerno.model
import de.pokerno.protocol.msg
import akka.actor.{Actor, ActorRef}

class Replayer(server: ActorRef) extends Actor {
  import context._

  var table: model.Table = null

  override def preStart {

  }
  
  override def receive = main

  def main: Receive = {
    case tags.Table(uuid, size) =>
      table = new model.Table(size)
      become(tableBlock)
    case tags.Speed(duration) =>
    case tags.Street(name) =>
      become(streetBlock)
    case _ =>

  }

  def streetBlock: Receive = {
    case tags.Sb(player) =>
    case tags.Bb(player) =>
    case tags.Ante(player) =>
    case tags.Raise(player, amount) =>
    case tags.AllIn(player) =>
    case tags.Call(player, amount) =>
    case tags.Fold(player) =>
    case tags.Deal(player, cards, cardsNum) =>
      
    case x: Any =>
      become(main)
      self ! x
  }

  def tableBlock: Receive = {
    case tags.Seat(uuid, stack) =>
      server ! rpc.JoinTable(uuid, table.button, stack)
      table.button.move
    case tags.Stake(sb, bb, ante) =>
    case tags.Game(variation, limit) =>
    case tags.Button(pos) =>
      table.button.current = pos
    case x: Any =>
      become(main)
      self ! x
  }

  override def postStop {

  }
}
