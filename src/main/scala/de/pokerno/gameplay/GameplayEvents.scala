package de.pokerno.gameplay

import de.pokerno.protocol.{msg => message}
import de.pokerno.model._
import de.pokerno.poker.{Card, Hand}

import math.{BigDecimal => Decimal}

class GameplayEvents {
  
  lazy val broker = new Broker
  
  type Box = Tuple2[Player, Int]
  
  def joinTable(box: Box, amount: Decimal) =
    broker.publish(
        message.JoinTable(box._2, box._1, amount)
    )
  
  def playStart =
    broker.publish(message.PlayStart())
  
  def playStop =
    broker.publish(message.PlayStop())
  
  def streetStart(name: Street.Value) =
    broker.publish(message.StreetStart(name))
  
  def dealCards(_type: DealCards.Value, cards: List[Card], box: Option[Box] = None) = _type match {
    case DealCards.Board =>
      broker.publish(
          message.DealCards(_type, cards)
        )
    
    case _ =>
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
    }
  
  def buttonChange(pos: Int) =
    broker.publish(
        message.ButtonChange(_button = pos)
      )
  
  def addBet(box: Box, bet: Bet) =
    broker.publish(
          message.AddBet(box._2, box._1, bet),
        broker.Except(List(box._1.id)))
  
  def requireBet(box: Box, call: Decimal, raise: Range) =
    broker.publish(
        message.RequireBet(pos = box._2, player = box._1, call = call, raise = raise)
      )
  
  def declarePot(total: Decimal) =
    broker.publish(message.DeclarePot(total))

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
}
