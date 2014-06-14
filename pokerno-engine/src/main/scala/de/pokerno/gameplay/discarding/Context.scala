package de.pokerno.gameplay.discarding

import akka.actor.{ActorRef, Cancellable}
import org.slf4j.LoggerFactory
import de.pokerno.gameplay.{Discarding, Context => Gameplay}
import de.pokerno.poker.Cards
import de.pokerno.model.Player

class Context(val gameplay: Gameplay, ref: ActorRef) extends Discarding {
  
  import gameplay._
  
  override def round = discardingRound
  
  private val log = LoggerFactory.getLogger(getClass)
  
  var timer: Option[Cancellable] = None
  
  def nextTurn(): Discarding.Transition = NextTurn.decide(round.seats.filter(_.inPot))
  
  def add(player: Player, cards: Cards) = round.discarding match {
    case Some(sitting) =>
      discardCards(sitting, cards)
      
    case None =>
  }
  
  def cancel() = round.discarding match {
    case Some(sitting) =>
      standPat(sitting)
      
    case none =>
  }
  
  def timeout() = round.discarding match {
    case Some(sitting) =>
      standPat(sitting)
    case None =>
  }
  
}
