package pokerno.backend.protocol

import pokerno.backend.poker.Card
import pokerno.backend.model._

object Message {
  trait Position {
    def pos: Option[Int]
  }
  
  trait Cards {
    def cards: List[Card]
  }
  
  case class DealCards (
    val dealing: Deal.Value,
    val pos: Option[Int],
    val cards: List[Card]
  ) extends Serializable with Cards with Position
  
  case class AddBet[T <: Bet.Type] (
    val pos: Option[Int],
    val bet: Bet[T]
  ) extends Serializable with Position 
}
