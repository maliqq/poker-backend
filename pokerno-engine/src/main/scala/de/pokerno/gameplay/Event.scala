package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.poker.{ Hand, Card }
import de.pokerno.protocol.{game_events => message}
import math.{BigDecimal => Decimal}

object Event {
  implicit def cards2binary(v: Seq[Card]): Array[Byte] = v.map(_.toByte).toArray
  
  def joinTable(pos: Int, player: Player, amount: Decimal) =
    message.JoinPlayer(pos, player, amount)

  def leaveTable(pos: Int, player: Player) =
    message.LeavePlayer(pos, player)

//  def start(table: Table, variation: Variation, stake: Stake, play: Play = null, connPlayer: Option[String] = None) = {
//    val msgPlay: message.Play = play
//
//    connPlayer.map { player ⇒
//      play.pocketCards(player) map { msgPlay.pocket = _ }
//    }
//
//    message.DeclareStart(table, variation, stake, connPlayer match {
//      case Some(player) => play.pocketCards(player) map { p => play.copy(pocket = p) }
//      case None => play
//    })
//  }

  def playStart() =
    message.DeclarePlayStart()

  def playStop() =
    message.DeclarePlayStop()

  def streetStart(name: Street.Value) =
    message.DeclareStreet(name)

  def dealCardsNum(pos: Int, player: Player, _type: DealType.Value, cards: Seq[Card]) = _type match {
    case DealType.Hole ⇒
      message.DealCards(_type, cardsNum = Some(cards.size), pos = Some(pos), player = Some(player))

    case _ ⇒ null
  }
  
  def dealCards(_type: DealType.Value, cards: Seq[Card]) = _type match {
    case DealType.Board =>
      message.DealCards(_type, cards)
  }

  def dealCards(pos: Int, player: Player, _type: DealType.Value, cards: Seq[Card]) = _type match {
    case DealType.Hole | DealType.Door ⇒
      message.DealCards(_type, cards, pos = Some(pos), player = Some(player))

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
    message.DeclareHand(pos, player, cards = cards, hand = hand)

  def gameChange(game: Game) =
    message.GameChange(game)

  def showCards(pos: Int, player: Player, cards: Seq[Card], muck: Boolean = false) =
    message.ShowCards(pos, player, cards = cards, muck = muck)

  def seatStateChanged(pos: Int, state: Seat.State.Value) =
    message.SeatEvent(message.SeatEventType.STATE, pos = pos, state = state)

  def seatPresenceChanged(pos: Int, presence: Seat.Presence.Value) =
    message.SeatEvent(message.SeatEventType.PRESENCE, pos = pos, presence = presence)
}
