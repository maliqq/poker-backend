package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

class StreetActor(val gameplay: Gameplay, val name: Street.Value, val stages: List[StreetStage]) extends Actor with ActorLogging {
  import context._
  val stagesIterator = stages.iterator

  override def preStart {
    Console printf ("%sstreet %s START%s\n", Console.YELLOW, name, Console.RESET)
  }

  def receive = {
    case Stage.Next ⇒
      if (stagesIterator hasNext) {
        log.info("stage start")
        val stage = stagesIterator.next
        stage.run(self)
      } else
        parent ! Street.Next

    case Street.Next ⇒
      parent ! Street.Next

    case Street.Exit ⇒
      parent ! Street.Exit
  }

  override def postStop {
    Console printf ("%sstreet %s STOP%s\n", Console.YELLOW, name, Console.RESET)
  }
}

case class Street(val name: Street.Value, val stages: List[Stage])

object Street extends Enumeration {
    
  case object Start
  case object Next
  case object Exit
  
  class StreetVal(i: Int, name: String) extends Val(i, name) {
    def apply(stages: List[Stage]): Street = new Street(this, stages)
  }
  
  def street(name: String) = new StreetVal(nextId, name)
  val Preflop = street("preflop")
  val Flop = street("flop")
  val Turn = street("turn")
  val River = street("river")
 
  val Second = street("second")
  val Third = street("third")
  val Fourth = street("fourth")
  val Fifth = street("fifth")
  val Sixth = street("sixth")
  val Seventh = street("seventh")
 
  val Predraw = street("predraw")
  val Draw = street("draw")
  val FirstDraw = street("first-draw")
  val SecondDraw = street("second-draw")
  val ThirdDraw = street("third-draw")
}

object Streets {
  def build(gameplay: Gameplay, bettingRef: ActorRef): List[Street] = {
    val discarding = DirectStreetStage("discarding") {}

    val bringIn = DirectStreetStage("bring-in") {
      gameplay.bringIn(bettingRef)
    }

    val bigBets = DirectStreetStage("big-bets") {
      bettingRef ! Betting.BigBets
    }

    val betting = BlockingStreetStage("betting") {
      bettingRef ! Betting.Next
    }

    def dealing(dealType: DealCards.Value, cardsNum: Option[Int] = None): Stage = DirectStreetStage("dealing") {
      gameplay.dealCards(dealType, cardsNum)
    }

    gameplay.game.options.group match {
      case Game.Holdem ⇒ List(
        Street.Preflop(
          List(dealing(DealCards.Hole), betting)),

        Street.Flop(
          List(dealing(DealCards.Board, Some(3)), betting)),

        Street.Turn(
          List(dealing(DealCards.Board, Some(1)), bigBets, betting)),

        Street.River(
          List(dealing(DealCards.Board, Some(1)), betting))
      )
      
      case Game.SevenCard ⇒ List(
        Street.Second(
          List(dealing(DealCards.Hole, Some(2)))),

        Street.Third(
          List(dealing(DealCards.Door, Some(1)), bringIn, betting)),

        Street.Fourth(
          List(dealing(DealCards.Door, Some(1)), betting)),

        Street.Fifth(
          List(dealing(DealCards.Door, Some(1)), bigBets, betting)),

        Street.Sixth(
          List(dealing(DealCards.Door, Some(1)), betting)),

        Street.Seventh(
          List(dealing(DealCards.Hole, Some(1)), betting))
      )
      
      case Game.SingleDraw ⇒ List(
        Street.Predraw(
          List(dealing(DealCards.Hole, Some(5)), betting, discarding)),

        Street.Draw(
          List(bigBets, betting, discarding))
      )
      
      case Game.TripleDraw ⇒ List(
        Street.Predraw(
          List(dealing(DealCards.Hole), betting, discarding)),

        Street.FirstDraw(
          List(betting, discarding)),

        Street.SecondDraw(
          List(bigBets, betting, discarding)),

        Street.ThirdDraw(
          List(betting, discarding))
      )
    }
  }
}
