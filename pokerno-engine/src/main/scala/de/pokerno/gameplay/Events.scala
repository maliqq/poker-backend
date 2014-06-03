package de.pokerno.gameplay

import de.pokerno.protocol.GameEvent

import de.pokerno.model._
import de.pokerno.poker.{ Hand, Card }
import de.pokerno.protocol.{game_events => message}
import math.{BigDecimal => Decimal}

class Events(id: String) {
  
  lazy val broker = new Broker(id)
  
  trait RouteBuilder {
    def all()               = Route.All
    def one(id: String)     = Route.One(id)
    def except(id: String)  = Route.Except(List(id))
    def only(id: String)    = Route.One(id)
  }
  
  object RouteBuilder extends RouteBuilder
  
  def publish(evt: GameEvent)(f: RouteBuilder => Route) {
    broker.publish(Notification(evt, from = Route.One(id), to = f(RouteBuilder)))
  }
  
}

object Events {
  def playerJoin(pos: Int, player: Player, amount: Decimal) =
    message.PlayerJoin(pos, player, amount)

  def playerLeave(pos: Int, player: Player) =
    message.PlayerLeave(pos, player)
  
  def playerOnline(pos: Int, player: Player) =
    message.PlayerOnline(pos, player)
  
  def playerOffline(pos: Int, player: Player) =
    message.PlayerOffline(pos, player)
    
  def playerSitOut(pos: Int, player: Player) =
    message.PlayerSitOut(pos, player)  
  
  def playerComeBack(pos: Int, player: Player) =
    message.PlayerComeBack(pos, player)
  
  def start(table: Table, variation: Variation, stake: Stake, play: Play = null, forPlayer: Option[String] = None) = {
//    val msg = message.DeclareStart(table, variation, stake)
//    forPlayer.map { player =>
//      msg.pocket
//    }
  }

  def playStart() =
    message.DeclarePlayStart()

  def playStop() =
    message.DeclarePlayStop()

  def streetStart(name: Street.Value) =
    message.DeclareStreet(name)

  def dealCardsNum(pos: Int, player: Player, _type: DealType.Value, cards: Seq[Card]) = _type match {
    case DealType.Hole ⇒ message.DealHole(pos, player, Right(cards.size))
    case _ ⇒ null
  }
  
  def dealCards(_type: DealType.Value, cards: Seq[Card]) = _type match {
    case DealType.Board => message.DealBoard(cards)
    case _ => null
  }

  def dealCards(pos: Int, player: Player, _type: DealType.Value, cards: Seq[Card]) = _type match {
    case DealType.Hole ⇒ message.DealHole(pos, player, Left(cards))
    case DealType.Door ⇒ message.DealDoor(pos, player, Left(cards))
    case _ ⇒ null
  }

  def buttonChange(pos: Int) =
    message.ButtonChange(pos)

  def addBet(pos: Int, player: Player, bet: Bet) =
    message.DeclareBet(pos, player, bet)

  def requireBet(pos: Int, player: Player, call: Decimal, raise: MinMax[Decimal]) = 
    message.AskBet(pos, player, call = call, raise = raise)

  def declarePot(total: Decimal, side: Seq[Decimal]) =
    message.DeclarePot(total, side)

  def declareWinner(pos: Int, player: Player, amount: Decimal) =
    message.DeclareWinner(pos, player, amount = amount)

  def declareHand(pos: Int, player: Player, cards: Seq[Card], hand: Hand) =
    message.DeclareHand(pos, player, hand)

  def gameChange(game: Game) =
    message.GameChange(game)

  def showCards(pos: Int, player: Player, cards: Seq[Card], muck: Boolean = false) =
    message.ShowCards(pos, player, cards = cards, muck = muck)
    
}
