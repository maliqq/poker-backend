package de.pokerno.gameplay.round

import akka.actor.{ActorRef, Cancellable}
import org.slf4j.LoggerFactory
import de.pokerno.gameplay.{Round, Context => Gameplay}
import de.pokerno.model.{Player, seat}

abstract class Context(val gameplay: Gameplay) {
  private val log = LoggerFactory.getLogger(getClass)
  
  import gameplay._
  
  def round: Round
  
  var timer: Option[Cancellable] = None
  
  def nextTurn(): Round.Transition
  def require(sitting: seat.Sitting): Unit
  def complete(): Unit
  def cancel(player: Player): Unit
  def timeout(): Unit
}
