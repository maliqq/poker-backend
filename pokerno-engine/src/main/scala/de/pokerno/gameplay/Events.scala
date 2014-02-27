package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{ wire, msg ⇒ message }
import de.pokerno.model._
import de.pokerno.poker.{ Card, Hand }

import protocol.Conversions._
import protocol.wire.Conversions._
import protocol.msg.Conversions._

import math.{ BigDecimal ⇒ Decimal }

object Events {
  type Box = Tuple2[Player, Int]
  
  case class EventWrap(msg: message.Message, route: Route = Route.All) {
    def except(id: String) = copy(route = Route.Except(List(id)))
    def only(id: String) = copy(route = Route.One(id))
  }

  def E(msg: message.Message) = EventWrap(msg) 
  
  def joinTable(box: Box, amount: Decimal) = E(
    message.PlayerJoin(box._2, box._1, amount)
  )
  
  def leaveTable(box: Box) = E(
    message.PlayerLeave(box._2, box._1)
  )
  
  def start(table: Table, variation: Variation, stake: Stake, play: Play) = E(
    message.Start(table, variation, stake, play)
  )
  
  def playStart(play: Play) = E(
    message.PlayStart(play)
  )

  def playStop() = E(
    message.PlayStop()
  )

  def streetStart(name: Street.Value) = E(
    message.StreetStart(name)
  )
  
  def dealCardsNum(_type: DealCards.Value, cards: List[Card], box: Option[Box]) = E(_type match {
    case DealCards.Hole if box.isDefined =>
      message.DealCards(_type, cardsNum = cards.size, player = box.get._1, pos = box.get._2)
    
    case _ => null
  })
  
  def dealCards(_type: DealCards.Value, cards: List[Card], box: Option[Box] = None) = E(_type match {
    case DealCards.Board ⇒
      message.DealCards(_type, cards)

    case _ if box.isDefined ⇒
      message.DealCards(_type, cards, player = box.get._1, pos = box.get._2)

    case _ ⇒ null
  })

  def buttonChange(pos: Int) = E(
    message.ButtonChange(_button = pos)
  )

  def addBet(box: Box, bet: Bet) = E(
    message.BetAdd(box._2, box._1, bet)
  )

  def requireBet(box: Box, call: Decimal, raise: Range) = E(
    message.RequireBet(pos = box._2, player = box._1, call = call, raise = raise)
  )

  def declarePot(total: Decimal, side: List[Decimal] = null) = {
    val result = new java.util.ArrayList[java.lang.Double]()
    for (amt ← side) result.add(amt.toDouble)
    E(message.DeclarePot(total, result))
  }

  def declareWinner(box: Box, amount: Decimal) = E(
    message.DeclareWinner(pos = box._2, player = box._1, amount = amount)
  )

  def declareHand(box: Box, cards: List[Card], hand: Hand) = E(
    message.DeclareHand(pos = box._2, player = box._1, cards = cards, hand = hand)
  )

  def gameChange(game: Game) = E(
    message.GameChange(_game = game)
  )

  def showCards(box: Box, cards: List[Card], muck: Boolean = false) = E(
    message.CardsShow(pos = box._2, player = box._1, cards = cards, muck = muck)
  )
  
  import proto.msg.SeatEventSchema
  def seatStateChanged(pos: Int, state: Seat.State.Value) = E(
    message.SeatEvent(SeatEventSchema.EventType.STATE, pos = pos, seat = new wire.Seat(state = state))
  )
  
  def seatPresenceChanged(pos: Int, presence: Seat.Presence.Value) = E(
    message.SeatEvent(SeatEventSchema.EventType.PRESENCE, pos = pos, seat = new wire.Seat(presence = presence))
  )
}

class Events(id: String) {
  lazy val broker = new Broker(id)
  
  def publish(e: Events.EventWrap) {
    broker.publish(Notification(e.msg, from = Route.One(id), to = e.route))
  }
}
