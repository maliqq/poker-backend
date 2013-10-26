package de.pokerno.backend.engine

import de.pokerno.backend.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }

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

object Street {
  case object Start
  case object Next
  case object Exit

  trait Value {
    def apply(stages: List[Stage]): Street = new Street(this, stages)
  }

  case object Preflop extends Value
  case object Flop extends Value
  case object Turn extends Value
  case object River extends Value

  case object Second extends Value
  case object Third extends Value
  case object Fourth extends Value
  case object Fifth extends Value
  case object Sixth extends Value
  case object Seventh extends Value

  case object Predraw extends Value
  case object Draw extends Value
  case object FirstDraw extends Value
  case object SecondDraw extends Value
  case object ThirdDraw extends Value
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

    def dealing(dealType: Dealer.DealType, cardsNum: Option[Int] = None): Stage = DirectStreetStage("dealing") {
      gameplay.dealCards(dealType, cardsNum)
    }

    gameplay.game.options.group match {
      case Game.Holdem ⇒ List(
        Street.Preflop(
          List(dealing(Dealer.Hole), betting)),

        Street.Flop(
          List(dealing(Dealer.Board, Some(3)), betting)),

        Street.Turn(
          List(dealing(Dealer.Board, Some(1)), bigBets, betting)),

        Street.River(
          List(dealing(Dealer.Board, Some(1)), betting)))

      case Game.SevenCard ⇒ List(
        Street.Second(
          List(dealing(Dealer.Hole, Some(2)))),

        Street.Third(
          List(dealing(Dealer.Door, Some(1)), bringIn, betting)),

        Street.Fourth(
          List(dealing(Dealer.Door, Some(1)), betting)),

        Street.Fifth(
          List(dealing(Dealer.Door, Some(1)), bigBets, betting)),

        Street.Sixth(
          List(dealing(Dealer.Door, Some(1)), betting)),

        Street.Seventh(
          List(dealing(Dealer.Hole, Some(1)), betting)))

      case Game.SingleDraw ⇒ List(
        Street.Predraw(
          List(dealing(Dealer.Hole, Some(5)), betting, discarding)),

        Street.Draw(
          List(bigBets, betting, discarding)))

      case Game.TripleDraw ⇒ List(
        Street.Predraw(
          List(dealing(Dealer.Hole), betting, discarding)),

        Street.FirstDraw(
          List(betting, discarding)),

        Street.SecondDraw(
          List(bigBets, betting, discarding)),

        Street.ThirdDraw(
          List(betting, discarding)))
    }
  }
}
