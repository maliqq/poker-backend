package de.pokerno.gameplay

import de.pokerno.protocol.GameEvent

import de.pokerno.model._
import de.pokerno.poker.{ Hand, Card, Cards }
import de.pokerno.protocol.msg

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
  def playStart(ctx: Context)         = msg.DeclarePlayStart(msg.PlayState(ctx))
  def playStop()                      = msg.DeclarePlayStop()
  def streetStart(name: Street.Value) = msg.DeclareStreet(name)
  def buttonChange(pos: Int)          = msg.ButtonChange(pos)
  def gameChange(game: Game)          = msg.GameChange(game)
  def declarePot(pot: Pot)            = msg.DeclarePot(pot)
  def dealBoard(cards: Cards)         = msg.DealBoard(cards)

  def playerJoin(sitting: seat.Sitting)      = msg.PlayerJoin(sitting.asPosition, sitting.stackAmount)
  def playerLeave(sitting: seat.Sitting)     = msg.PlayerLeave(sitting.asPosition)
  def playerOnline(sitting: seat.Sitting)    = msg.PlayerOnline(sitting.asPosition)
  def playerOffline(sitting: seat.Sitting)   = msg.PlayerOffline(sitting.asPosition)
  def playerSitOut(sitting: seat.Sitting)    = msg.PlayerSitOut(sitting.asPosition)
  def playerComeBack(sitting: seat.Sitting)  = msg.PlayerComeBack(sitting.asPosition)
  
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

  def dealPocket(sitting: seat.Sitting, _type: DealType.Value, cards: Cards) = _type match {
    case DealType.Hole => msg.DealHole(sitting.asPosition, Left(cards))
    case DealType.Door => msg.DealDoor(sitting.asPosition, Left(cards))
  }
  def dealPocketNum(sitting: seat.Sitting, _type: DealType.Value, n: Int) = _type match {
    case DealType.Hole => msg.DealHole(sitting.asPosition, Right(n))
    case DealType.Door => msg.DealDoor(sitting.asPosition, Right(n))
  }
  
  def discardCards(sitting: seat.Sitting, cards: Cards) =
    msg.DiscardCards(sitting.asPosition, Left(cards))
  
  def discardCardsNum(sitting: seat.Sitting, cardsNum: Int) =
    msg.DiscardCards(sitting.asPosition, Right(cardsNum))
  
  def requireBet(sitting: seat.Sitting) = msg.AskBet(sitting.asActing)
  def requireDiscard(sitting: seat.Sitting) = msg.AskDiscard(sitting.asPosition)
  
  def addBet(sitting: seat.Sitting, bet: Bet, timeout: Option[Boolean] = None)
                                = msg.DeclareBet(sitting.asPosition, bet, timeout)
  def declareWinner(sitting: seat.Sitting, amount: Decimal)
                                = msg.DeclareWinner(sitting.asPosition, amount)
  def declareHand(sitting: seat.Sitting, cards: Cards, hand: Hand)
                                = msg.DeclareHand(sitting.asPosition, cards, hand)
  def showCards(sitting: seat.Sitting, cards: Cards, muck: Boolean = false)
                                = msg.ShowCards(sitting.asPosition, cards, muck)
    
}
