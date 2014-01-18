package de.pokerno.playground

import de.pokerno.gameplay.Instance
import de.pokerno.format.text.Lexer.{Token, Tags => tags}
import de.pokerno.model.{Player, Table, Stake, Variation, Game, Bet, Seat}
import de.pokerno.protocol._
import wire.Conversions._
import akka.actor.{Actor, ActorSystem, ActorLogging, ActorRef, Props}

class Replayer(system: ActorSystem) {
  
  var table: Option[Table] = None
  var stake: Option[Stake] = None
  var variation: Option[Variation] = None
  var id: Option[String] = None
  var speed: Option[Int] = None
  var node: ActorRef = system.deadLetters
  
  var processor: Function1[Token, Unit] = processMain
  
  def process(t: Token) = processor(t)
  
  def processMain(t: Token): Unit = t match {
    case tags.Table(_id, size) =>
      table = Some(new Table(size))
      id = Some(_id.unquote)
      processor = processTable
      
    case tags.Speed(duration) =>
      speed = Some(duration)
      
    case tags.Street(name) =>
      processor = processStreet
      
    case _ =>
  }
  
  def bet(player: String, bet: Bet): Unit = table.map { t =>
    t.seats.asInstanceOf[List[Seat]].zipWithIndex.filter (_._1.player.get.toString == player).map { case (seat, pos) =>
      node ! rpc.AddBet(seat.player.get, bet)
    }
  }

  def processStreet(t: Token) = t match {
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
      
    case x: Any => processor = processMain
  }

  def processTable(t: Token) = t match {
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
      node = system.actorOf(Props(classOf[Instance], variation.get, stake.get))
      node ! Instance.Start
      
      table.get.seats.asInstanceOf[List[Seat]].zipWithIndex.map { case (seat, pos) =>
        if (!seat.isEmpty) {
          node ! msg.JoinTable(pos, seat.player.get, seat.stack)
        }
      }
      
      processor = processMain
      process(x)
  }
  
}
