package de.pokerno.playground

import de.pokerno.format.text.Lexer.{Tags => tags}
import de.pokerno.model.{Player, Table, Stake, Variation, Game, Bet}
import de.pokerno.protocol._
import wire.Conversions._
import akka.actor.{Actor, ActorLogging, ActorRef}

class Replayer(node: ActorRef) extends Actor with ActorLogging {
  import context._
  
  var table: Option[Table] = None
  var stake: Option[Stake] = None
  var variation: Option[Variation] = None
  var id: Option[String] = None
  var speed: Option[Int] = None
  
  override def preStart {
  }
  
  override def receive = main

  def main: Receive = {
    case x: Any =>
      Console printf("x=%s\n", x)
      x match {
      case tags.Table(_id, size) =>
      table = Some(new Table(size))
      id = Some(_id.unquote)
      println("tableBlock")
      become(tableBlock)
      
    case tags.Speed(duration) =>
      speed = Some(duration)
      
    case tags.Street(name) =>
      println("streetBlock")
      become(streetBlock)
      
    case _ =>

  }}
  
  def bet(player: String, bet: Bet) {
    Console printf("BET: %s", bet)
    table.map { t =>
      t.seats.where(_.player.get.toString == player).map { case (seat, pos) =>
        node ! rpc.AddBet(seat.player.get, bet)
      }
    }
  }

  def streetBlock: Receive = {case x: Any =>
    Console printf("x=%s\n", x)
    x match {
    case tags.Sb(player) =>
      bet(player.unquote, Bet.sb(stake.get.smallBlind))
      
    case tags.Bb(player) =>
      bet(player.unquote, Bet.bb(stake.get.smallBlind))
      
    case tags.Ante(player) =>
      bet(player.unquote, Bet.ante(stake.get.smallBlind))
      
    case tags.Raise(player, amount) =>
      bet(player.unquote, Bet.raise(amount))
      
    case tags.AllIn(player) =>
      bet(player.unquote, Bet.allin)
      
    case tags.Call(player, amount) =>
      bet(player.unquote, Bet.call(amount))
      
    case tags.Fold(player) =>
      bet(player.unquote, Bet.fold)
      
    case tags.Deal(player, cards, cardsNum) =>
      
    case x: Any =>
      println("main from streetBlock")
      self ! x
      become(main)
  }}

  def tableBlock: Receive = {case x:Any =>
    Console printf("x=%s\n", x)
    x match {
    case tags.Seat(uuid, stack) =>
      table.map { t =>
        val player = Player(uuid.unquote)
        t.addPlayer(player, t.button, Some(stack))
        //node ! rpc.JoinPlayer(player, t.button, Some(stack))
        t.button.move
      }
      
    case tags.Stake(sb, bb, ante) =>
      stake = Some(Stake(sb,
          Some(bb),
          Ante = ante match {
            case Some(n) => Left(n)
            case None => Right(false)
          }))
          
    case tags.Game(game, limit) =>
      variation = Some(Game(game, limit))
      
    case tags.Button(pos) =>
      table.map(_.button.current = pos)
      
    case x: Any =>
      node ! rpc.CreateRoom(id.get,
          table = wire.Table(table.size),
          variation = variation.get,
          stake = stake.get)
      println("main from tableBlock")
      self ! x
      become(main)
  }}

  override def postStop {

  }
}
