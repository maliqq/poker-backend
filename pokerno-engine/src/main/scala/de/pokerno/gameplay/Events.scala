package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{ wire, msg ⇒ message }
import de.pokerno.model._
import de.pokerno.poker.{ Card, Hand }

import math.{ BigDecimal ⇒ Decimal }

class Events(id: String) {

  import protocol.Conversions._
  import protocol.wire.Conversions._
  import protocol.msg.Conversions._

  lazy val broker = new Broker(id)

  type Box = Tuple2[Player, Int]

  def joinTable(box: Box, amount: Decimal) =
    broker.publish(Notification(
      message.PlayerJoin(box._2, box._1, amount)
    ))

  def leaveTable(box: Box) =
    broker.publish(Notification(
      message.PlayerLeave(box._2, box._1)
    ))

  def start(player: Player, table: Table, variation: Variation, stake: Stake, play: Play) = {
    broker.publish(Notification(
      message.Start(table, variation, stake, play),
      to = Route.One(player)
    ))
  }

  def playStart() =
    broker.publish(Notification(
      message.PlayStart()
    ))

  def playStop() =
    broker.publish(Notification(
      message.PlayStop()
    ))

  def streetStart(name: Street.Value) =
    broker.publish(Notification(
      message.StreetStart(name)
    ))

  def dealCards(_type: DealCards.Value, cards: List[Card], box: Option[Box] = None) = _type match {
    case DealCards.Board ⇒
      broker.publish(Notification(
        message.DealCards(_type, cards)
      ))

    case _ if box.isDefined ⇒
      if (_type == DealCards.Hole) {
        val player = box.get._1
        broker.publish(Notification(
          message.DealCards(_type, player = player, pos = box.get._2,
            cards = cards
          ),
          to = Route.One(player.id)
        ))
        broker.publish(Notification(
          message.DealCards(_type, cardsNum = cards.size, player = box.get._1, pos = box.get._2),
          to = Route.Except(List(player.id))
        ))
      } else {
        broker.publish(Notification(
          message.DealCards(_type, cards, player = box.get._1, pos = box.get._2)
        ))
      }

    case _ ⇒
  }

  def buttonChange(pos: Int) =
    broker.publish(Notification(
      message.ButtonChange(_button = pos)
    ))

  def addBet(box: Box, bet: Bet) =
    broker.publish(Notification(
      message.BetAdd(box._2, box._1, bet)
    ))

  def requireBet(box: Box, call: Decimal, raise: Range) =
    broker.publish(Notification(
      message.RequireBet(pos = box._2, player = box._1, call = call, raise = raise)
    ))

  def declarePot(total: Decimal, side: List[Decimal] = null) = {
    val result = new java.util.ArrayList[java.lang.Double]()
    for (amt ← side) result.add(amt.toDouble)
    broker.publish(Notification(
      message.DeclarePot(total, result)
    ))
  }

  def declareWinner(box: Box, amount: Decimal) =
    broker.publish(Notification(
      message.DeclareWinner(pos = box._2, player = box._1, amount = amount)
    ))

  def declareHand(box: Box, cards: List[Card], hand: Hand) =
    broker.publish(Notification(
      message.DeclareHand(pos = box._2, player = box._1, cards = cards, hand = hand)
    ))

  def gameChange(game: Game) =
    broker.publish(Notification(
      message.GameChange(_game = game)
    ))

  def showCards(box: Box, cards: List[Card], muck: Boolean = false) =
    broker.publish(Notification(
      message.CardsShow(pos = box._2, player = box._1, cards = cards, muck = muck)
    ))

  import proto.msg.SeatEventSchema
  def seatStateChanged(pos: Int, state: Seat.State.Value) =
    broker.publish(Notification(
      message.SeatEvent(SeatEventSchema.EventType.STATE, pos = pos, seat = new wire.Seat(state = state))
    ))

}
