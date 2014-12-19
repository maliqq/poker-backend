package de.pokerno.gameplay

import de.pokerno.protocol.GameEvent

import de.pokerno.model._
import de.pokerno.model.tournament._
import de.pokerno.model.seat.impl.Sitting
import de.pokerno.model.seat.impl.{Change => SeatChange}
import de.pokerno.poker.{ Hand, Card, Cards }
import de.pokerno.protocol.msg

object Events {
  
  def announceStart(after: concurrent.duration.FiniteDuration) = msg.AnnounceStart(after.toSeconds) 
  
  def playStart(ctx: Context)         = msg.DeclarePlayStart(msg.PlayState(ctx))
  def playStop()                      = msg.DeclarePlayStop()
  def dealCancel()                    = msg.DeclareDealCancel()
  def streetStart(name: Street.Value) = msg.DeclareStreet(name)
  def buttonChange(pos: Int)          = msg.ButtonChange(pos)
  def gameChange(game: Game)          = msg.GameChange(game)
  def declarePot(pot: Pot)            = msg.DeclarePot(pot)
  def dealBoard(cards: Cards)         = msg.DealBoard(cards)

  def playerLeave(seat: Sitting, kick: Boolean) = msg.PlayerLeave(seat.asPosition, if (kick) Some(true) else None)
  def playerJoin(seat: Sitting)      = msg.PlayerJoin(seat.asPosition, seat.stackAmount)
  def playerOnline(seat: Sitting)    = msg.PlayerOnline(seat.asPosition)
  def playerOffline(seat: Sitting)   = msg.PlayerOffline(seat.asPosition)
  def playerSitOut(seat: Sitting)    = msg.PlayerSitOut(seat.asPosition)
  def playerComeBack(seat: Sitting)  = msg.PlayerComeBack(seat.asPosition)
  
  def start(id: String, state: String, table: Table, variation: Variation, stake: Stake, player: Option[Player] = None) =
    msg.DeclareStart(
      id,
      state,
      table,
      variation,
      stake,
      player)
  
  def start(ctx: Context, state: String, player: Option[Player]) = {
    // build start message for player or watcher
    val start = msg.DeclareStart(
        ctx.id,
        state,
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
  def notice(message: String)     = msg.Notice(message)

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
  def requireRebuy(seat: Sitting, stake: Stake, available: Decimal) = {
    val (min, max) = stake.buyInAmount
    msg.AskBuyIn(seat.asPosition, ((min - seat.stackAmount).abs, max - seat.stackAmount), available)
  }

  def stackChange(seat: Sitting) = {
    val change = SeatChange.from(seat)
    change.stack = seat.stack
    msg.SeatChange(change)
  }

  def addBet(seat: Sitting, bet: Bet, timeout: Option[Boolean] = None) = msg.DeclareBet(seat.asPosition, bet, timeout)
  def declareWinner(seat: Sitting, amount: Decimal, uncalled: Option[Boolean] = None) = msg.DeclareWinner(seat.asPosition, amount, uncalled)
  def declareHand(seat: Sitting, cards: Cards, hand: Hand) = msg.DeclareHand(seat.asPosition, cards, hand)
  def showCards(seat: Sitting, cards: Cards, muck: Boolean = false) = msg.ShowCards(seat.asPosition, cards, muck)

  // tournaments
  def levelUp(number: Int, level: Level) = msg.LevelUp(number, level)
}
