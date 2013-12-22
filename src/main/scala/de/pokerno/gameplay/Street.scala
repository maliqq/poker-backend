package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.backend.{protocol => proto}

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
  
  type Value = proto.StageEventSchema.StreetType
  
  final val Preflop: Value = proto.StageEventSchema.StreetType.PREFLOP
  final val Flop: Value = proto.StageEventSchema.StreetType.FLOP
  final val Turn: Value = proto.StageEventSchema.StreetType.TURN
  final val River: Value = proto.StageEventSchema.StreetType.RIVER

  final val Second: Value = proto.StageEventSchema.StreetType.SECOND
  final val Third: Value = proto.StageEventSchema.StreetType.THIRD
  final val Fourth: Value = proto.StageEventSchema.StreetType.FOURTH
  final val Fifth: Value = proto.StageEventSchema.StreetType.FIFTH
  final val Sixth: Value = proto.StageEventSchema.StreetType.SIXTH
  final val Seventh: Value = proto.StageEventSchema.StreetType.SEVENTH

  final val Predraw: Value = proto.StageEventSchema.StreetType.PREDRAW
  final val Draw: Value = proto.StageEventSchema.StreetType.DRAW
  final val FirstDraw: Value = proto.StageEventSchema.StreetType.FIRST_DRAW
  final val SecondDraw: Value = proto.StageEventSchema.StreetType.SECOND_DRAW
  final val ThirdDraw: Value = proto.StageEventSchema.StreetType.THIRD_DRAW
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
        Street(Street.Preflop,
          List(dealing(DealCards.Hole), betting)),

        Street(Street.Flop,
          List(dealing(DealCards.Board, Some(3)), betting)),

        Street(Street.Turn,
          List(dealing(DealCards.Board, Some(1)), bigBets, betting)),

        Street(Street.River,
          List(dealing(DealCards.Board, Some(1)), betting)))

      case Game.SevenCard ⇒ List(
        Street(Street.Second,
          List(dealing(DealCards.Hole, Some(2)))),

        Street(Street.Third,
          List(dealing(DealCards.Door, Some(1)), bringIn, betting)),

        Street(Street.Fourth,
          List(dealing(DealCards.Door, Some(1)), betting)),

        Street(Street.Fifth,
          List(dealing(DealCards.Door, Some(1)), bigBets, betting)),

        Street(Street.Sixth,
          List(dealing(DealCards.Door, Some(1)), betting)),

        Street(Street.Seventh,
          List(dealing(DealCards.Hole, Some(1)), betting)))

      case Game.SingleDraw ⇒ List(
        Street(Street.Predraw,
          List(dealing(DealCards.Hole, Some(5)), betting, discarding)),

        Street(Street.Draw,
          List(bigBets, betting, discarding)))

      case Game.TripleDraw ⇒ List(
        Street(Street.Predraw,
          List(dealing(DealCards.Hole), betting, discarding)),

        Street(Street.FirstDraw,
          List(betting, discarding)),

        Street(Street.SecondDraw,
          List(bigBets, betting, discarding)),

        Street(Street.ThirdDraw,
          List(betting, discarding)))
    }
  }
}
