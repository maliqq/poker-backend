package de.pokerno.gameplay

import de.pokerno.protocol.GameEvent

import de.pokerno.model._
import de.pokerno.model.tournament._
import de.pokerno.model.table.seat.Sitting
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
  
  def announceStart(after: concurrent.duration.FiniteDuration) = msg.AnnounceStart(after.toSeconds) 
  
  def playStart(ctx: Context)         = msg.DeclarePlayStart(msg.PlayState(ctx))
  def playStop()                      = msg.DeclarePlayStop()
  def streetStart(name: Street.Value) = msg.DeclareStreet(name)
  def buttonChange(pos: Int)          = msg.ButtonChange(pos)
  def gameChange(game: Game)          = msg.GameChange(game)
  def declarePot(pot: Pot)            = msg.DeclarePot(pot)
  def dealBoard(cards: Cards)         = msg.DealBoard(cards)

  def playerJoin(seat: Sitting)      = msg.PlayerJoin(seat.asPosition, seat.stackAmount)
  def playerLeave(seat: Sitting)     = msg.PlayerLeave(seat.asPosition)
  def playerOnline(seat: Sitting)    = msg.PlayerOnline(seat.asPosition)
  def playerOffline(seat: Sitting)   = msg.PlayerOffline(seat.asPosition)
  def playerSitOut(seat: Sitting)    = msg.PlayerSitOut(seat.asPosition)
  def playerComeBack(seat: Sitting)  = msg.PlayerComeBack(seat.asPosition)
  
  def start(id: String, table: Table, variation: Variation, stake: Stake, player: Option[Player] = None) =
    msg.DeclareStart(
      id,
      table,
      variation,
      stake,
      player)
  
  def start(ctx: Context, player: Option[Player]) = {
    // build start message for player or watcher
    val start = msg.DeclareStart(
        ctx.id,
        ctx.table,
        ctx.variation,
        ctx.stake,
        player)
    val play = ctx.play.copy()
    start.play = Some(msg.PlayState(ctx))
    // include information on own cards
    player map { p =>
      start.pocket = ctx.dealer.pocket(p)
    }
    start
  }
  
  def error(err: String)      = msg.Error(err)
  def error(err: Throwable)   = msg.Error(err.getMessage)

  def dealPocket(seat: Sitting, _type: DealType.Value, cards: Cards) = _type match {
    case DealType.Hole => msg.DealHole(seat.asPosition, Left(cards))
    case DealType.Door => msg.DealDoor(seat.asPosition, Left(cards))
  }
  def dealPocketNum(seat: Sitting, _type: DealType.Value, n: Int) = _type match {
    case DealType.Hole => msg.DealHole(seat.asPosition, Right(n))
    case DealType.Door => msg.DealDoor(seat.asPosition, Right(n))
  }
  
  def discardCards(seat: Sitting, cards: Cards) =
    msg.DiscardCards(seat.asPosition, Left(cards))
  
  def discardCardsNum(seat: Sitting, cardsNum: Int) =
    msg.DiscardCards(seat.asPosition, Right(cardsNum))
  
  def requireBet(seat: Sitting) = msg.AskBet(seat.asActing)
  def requireDiscard(seat: Sitting) = msg.AskDiscard(seat.asPosition)
  def requireBuyIn(seat: Sitting, stake: Stake, available: Decimal) = {
    msg.AskBuyIn(seat.asPosition, stake.buyInAmount, available)
  }
  
  def addBet(seat: Sitting, bet: Bet, timeout: Option[Boolean] = None) = msg.DeclareBet(seat.asPosition, bet, timeout)
  def declareWinner(seat: Sitting, amount: Decimal, uncalled: Option[Boolean] = None) = msg.DeclareWinner(seat.asPosition, amount, uncalled)
  def declareHand(seat: Sitting, cards: Cards, hand: Hand) = msg.DeclareHand(seat.asPosition, cards, hand)
  def showCards(seat: Sitting, cards: Cards, muck: Boolean = false) = msg.ShowCards(seat.asPosition, cards, muck)
    
  // tournaments
  def levelUp(number: Int, level: Level) = msg.LevelUp(number, level)
}
