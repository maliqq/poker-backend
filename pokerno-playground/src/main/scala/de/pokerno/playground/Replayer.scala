package de.pokerno.playground

import de.pokerno.format.text.Lexer.{Tags => tags}
import de.pokerno.model
import de.pokerno.protocol._
import akka.actor.{Actor, ActorRef}

class Replayer(node: ActorRef) extends Actor {
  import context._

  var table: Option[model.Table] = None
  var stake: Option[model.Stake] = None
  var id: Option[String] = None
  var speed: Option[Int] = None
  var variation: Option[model.Variation] = None
  var limit: Option[model.Game.Limit] = None

  override def preStart {

  }
  
  override def receive = main

  def main: Receive = {
    case tags.Table(_id, size) =>
      table = Some(new model.Table(size))
      id = Some(_id.unquote)
      become(tableBlock)
      
    case tags.Speed(duration) =>
      speed = Some(duration)
      
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

  import wire.Conversions._
  
  def tableBlock: Receive = {
    case tags.Seat(uuid, stack) =>
      table.map(_.button.move)
      
    case tags.Stake(sb, bb, ante) =>
      stake = Some(model.Stake(sb,
          Some(bb),
          Ante = ante match {
            case Some(n) => Left(n)
            case None => Right(false)
          }))
          
    case tags.Game(_variation, _limit) =>
      variation = Some(_variation)
      limit = Some(_limit)
      
    case tags.Button(pos) =>
      table.map(_.button.current = pos)
      
    case x: Any =>
      node ! rpc.CreateRoom(id.get,
          table = wire.Table(table.size),
          variation = variation.get,
          stake = stake.get)
      become(main)
      self ! x
  }

  override def postStop {

  }
}
