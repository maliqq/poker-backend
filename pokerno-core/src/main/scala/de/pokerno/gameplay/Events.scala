package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{ wire, msg ⇒ message }
import de.pokerno.model._
import de.pokerno.poker.{ Card, Hand }

import math.{ BigDecimal ⇒ Decimal }

class Events {

  import protocol.Conversions._
  import protocol.wire.Conversions._
  import protocol.msg.Conversions._

  lazy val broker = new Broker

  type Box = Tuple2[Player, Int]

  def joinTable(box: Box, amount: Decimal) =
    broker.publish(
      message.PlayerJoin(box._2, box._1, amount)
    )

  def start(table: Table, variation: Variation, stake: Stake, play: Play) = {
    broker.publish(
      message.Start(table, variation, stake, play)
    )
  }

  def playStart() =
    broker.publish(message.PlayStart())

  def playStop() =
    broker.publish(message.PlayStop())

  def streetStart(name: Street.Value) =
    broker.publish(message.StreetStart(name))

  def dealCards(_type: DealCards.Value, cards: List[Card], box: Option[Box] = None) = _type match {
    case DealCards.Board ⇒
      broker.publish(
        message.DealCards(_type, cards)
      )

    case _ if box.isDefined ⇒
      if (_type == DealCards.Hole) {
        //        broker.publish(
        //            message.DealCards(_type, cards, pos = pos),
        //          broker.One(seat.player.get.id))

        broker.publish(
          message.DealCards(_type, player = box.get._1, pos = box.get._2,
            cards = cards // FIXME hide later
          )
        )
      } else broker.publish(message.DealCards(_type, cards, player = box.get._1, pos = box.get._2))
    case _ ⇒
  }

  def buttonChange(pos: Int) =
    broker.publish(
      message.ButtonChange(_button = pos)
    )

  def addBet(box: Box, bet: Bet) =
    broker.publish(
      message.BetAdd(box._2, box._1, bet),
      broker.Except(List(box._1.id)))

  def requireBet(box: Box, call: Decimal, raise: Range) =
    broker.publish(
      message.RequireBet(pos = box._2, player = box._1, call = call, raise = raise)
    )

  def declarePot(total: Decimal, side: List[Decimal] = null) = {
    val result = new java.util.ArrayList[java.lang.Double]()
    for (amt ← side) result.add(amt.toDouble)
    broker.publish(message.DeclarePot(total, result))
  }

  def declareWinner(box: Box, amount: Decimal) =
    broker.publish(
      message.DeclareWinner(pos = box._2, player = box._1, amount = amount)
    )

  def declareHand(box: Box, cards: List[Card], hand: Hand) =
    broker.publish(
      message.DeclareHand(pos = box._2, player = box._1, cards = cards, hand = hand)
    )

  def gameChange(game: Game) =
    broker.publish(
      message.GameChange(_game = game)
    )

  def showCards(box: Box, cards: List[Card], muck: Boolean = false) =
    broker.publish(
      message.CardsShow(pos = box._2, player = box._1, cards = cards, muck = muck)
    )

  def seatStateChanged(pos: Int, state: Seat.State.Value) =
    broker.publish(
      message.SeatEvent(message.SeatEventSchema.EventType.STATE, pos = pos, seat = new wire.Seat(state = state))
    )

}
