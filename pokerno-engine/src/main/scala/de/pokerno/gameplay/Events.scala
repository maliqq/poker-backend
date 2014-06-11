package de.pokerno.gameplay

import de.pokerno.protocol.GameEvent

import de.pokerno.model._
import de.pokerno.poker.{ Hand, Card, Cards }
import de.pokerno.protocol.msg
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

  def broadcast(evt: GameEvent) {
    broker.publish(Notification(evt, from = Route.One(id), to = Route.All))
  }
  
}

object Events {
  def playerJoin(seat: Seat) =
    msg.PlayerJoin(seat.pos, seat.player.get, seat.stackAmount)

  def playerLeave(seat: Seat) =
    msg.PlayerLeave(seat.pos, seat.player.get)
  
  def playerOnline(seat: Seat) =
    msg.PlayerOnline(seat.pos, seat.player.get)
  
  def playerOffline(seat: Seat) =
    msg.PlayerOffline(seat.pos, seat.player.get)
    
  def playerSitOut(seat: Seat) =
    msg.PlayerSitOut(seat.pos, seat.player.get)  
  
  def playerComeBack(seat: Seat) =
    msg.PlayerComeBack(seat.pos, seat.player.get)
  
  def start(id: String, table: Table, variation: Variation, stake: Stake) = msg.DeclareStart(id, table, variation, stake)
  def start(ctx: Context, player: Option[Player]) = {
    // build start message for player or watcher
    val start = msg.DeclareStart(ctx.id, ctx.table, ctx.variation, ctx.stake)
    val play = ctx.play.copy()
    start.play = Some(msg.PlayState(ctx))
    // include information on own cards
    player map { p =>
      start.pocket = ctx.dealer.pocket(p)
    }
    start
  }

  def playStart(ctx: Context) =
    msg.DeclarePlayStart(msg.PlayState(ctx))

  def playStop() =
    msg.DeclarePlayStop()

  def streetStart(name: Street.Value) =
    msg.DeclareStreet(name)

  def dealBoard(cards: Cards) = msg.DealBoard(cards)
  def dealPocket(seat: Seat, _type: DealType.Value, cards: Cards) = _type match {
    case DealType.Hole => msg.DealHole(seat.pos, seat.player.get, Left(cards))
    case DealType.Door => msg.DealDoor(seat.pos, seat.player.get, Left(cards))
  }
  def dealPocketNum(seat: Seat, _type: DealType.Value, n: Int) = _type match {
    case DealType.Hole => msg.DealHole(seat.pos, seat.player.get, Right(n))
    case DealType.Door => msg.DealDoor(seat.pos, seat.player.get, Right(n))
  }
  
  def askBuyIn {
    
  }
  
  def buttonChange(pos: Int) =
    msg.ButtonChange(pos)

  def addBet(seat: Seat, bet: Bet, timeout: Option[Boolean] = None) =
    msg.DeclareBet(seat.pos, seat.player.get, bet, timeout)

  def requireBet(seat: Seat) = 
    msg.AskBet(seat.pos, seat.player.get, seat.call.get, seat.raise)

  def declarePot(pot: Pot) =
    msg.DeclarePot(pot)

  def declareWinner(seat: Seat, amount: Decimal) =
    msg.DeclareWinner(seat.pos, seat.player.get, amount = amount)

  def declareHand(seat: Seat, cards: Cards, hand: Hand) =
    msg.DeclareHand(seat.pos, seat.player.get, cards, hand)

  def gameChange(game: Game) =
    msg.GameChange(game)

  def showCards(seat: Seat, cards: Cards, muck: Boolean = false) =
    msg.ShowCards(seat.pos, seat.player.get, cards = cards, muck = muck)
    
}
