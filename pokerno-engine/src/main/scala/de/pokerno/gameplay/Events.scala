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
  def playerJoin(pos: Int, player: Player, amount: Decimal) =
    msg.PlayerJoin(pos, player, amount)

  def playerLeave(pos: Int, player: Player) =
    msg.PlayerLeave(pos, player)
  
  def playerOnline(pos: Int, player: Player) =
    msg.PlayerOnline(pos, player)
  
  def playerOffline(pos: Int, player: Player) =
    msg.PlayerOffline(pos, player)
    
  def playerSitOut(pos: Int, player: Player) =
    msg.PlayerSitOut(pos, player)  
  
  def playerComeBack(pos: Int, player: Player) =
    msg.PlayerComeBack(pos, player)
  
  def start(table: Table, variation: Variation, stake: Stake) = msg.DeclareStart(table, variation, stake)
  def start(ctx: Context, player: Option[Player]) = {
    // build start message for player or watcher
    val start = msg.DeclareStart(ctx.table, ctx.variation, ctx.stake)
    val play = ctx.play.copy()
    start.play = Some(msg.PlayState(ctx))
    // include information on own cards
    player map { p =>
      start.pocket = ctx.dealer.pocketOption(p) orNull // FIXME null bullshit
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
  def dealPocket(pos: Int, player: Player, _type: DealType.Value, cards: Cards) = _type match {
    case DealType.Hole => msg.DealHole(pos, player, Left(cards))
    case DealType.Door => msg.DealDoor(pos, player, Left(cards))
  }
  def dealPocketNum(pos: Int, player: Player, _type: DealType.Value, n: Int) = _type match {
    case DealType.Hole => msg.DealHole(pos, player, Right(n))
    case DealType.Door => msg.DealDoor(pos, player, Right(n))
  }
  
  def buttonChange(pos: Int) =
    msg.ButtonChange(pos)

  def addBet(pos: Int, player: Player, bet: Bet) =
    msg.DeclareBet(pos, player, bet)

  def requireBet(acting: betting.Acting) = 
    msg.AskBet(acting)

  def declarePot(total: Decimal, side: Seq[Decimal]) =
    msg.DeclarePot(total, side)

  def declareWinner(pos: Int, player: Player, amount: Decimal) =
    msg.DeclareWinner(pos, player, amount = amount)

  def declareHand(pos: Int, player: Player, cards: Cards, hand: Hand) =
    msg.DeclareHand(pos, player, hand)

  def gameChange(game: Game) =
    msg.GameChange(game)

  def showCards(pos: Int, player: Player, cards: Cards, muck: Boolean = false) =
    msg.ShowCards(pos, player, cards = cards, muck = muck)
    
}
