package de.pokerno.gameplay.discarding

import akka.actor.{ActorRef, Cancellable}

import de.pokerno.gameplay.{Discarding, Context => Gameplay, Round => GameplayRound}
import de.pokerno.gameplay.round.{Context => RoundContext}
import de.pokerno.poker.Cards
import de.pokerno.model.Player

private[gameplay] class Context(_gameplay: Gameplay, ref: ActorRef) extends RoundContext(_gameplay) with Discarding {
  
  import gameplay._
  
  override def round = discardingRound
  
  def nextTurn(): GameplayRound.Transition = NextTurn.decide(round.seats.filter(_.inPot))
  
  def discard(player: Player, cards: Cards) = round.acting match {
    case Some(seat) if seat.player == player =>
      timer.map(_.cancel())
      discardCards(seat, cards)
      ref ! nextTurn()
    
    case _ =>
      log.info("not our turn: {}", round.acting)
  }
  
  def cancel(player: Player) = round.acting match {
    case Some(seat) if seat.player == player =>
      // TODO player left
    case _ =>
  }
  
  def timeout() = round.acting match {
    case Some(seat) =>
      standPat(seat)
      ref ! nextTurn()
      
    case _ =>
  }
  
}
