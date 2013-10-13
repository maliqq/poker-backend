package pokerno.backend.engine

import pokerno.backend.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }

class StreetActor(val gameplay: Gameplay, val name: Street.Value, val stages: List[Stage]) extends Actor with ActorLogging {
  import context._
  val stagesIterator = stages.iterator

  override def preStart {
    log.info("street %s START" format (name))
  }

  def receive = {
    case Stage.Next ⇒
      if (stagesIterator hasNext) {
        log.info("stage start")
        val stage = stagesIterator.next
        stage proceed (Stage.Context(gameplay = gameplay, street = self))
      } else
        parent ! Street.Next

    case Street.Next ⇒ parent ! Street.Next
    case Street.Exit ⇒ parent ! Street.Exit
  }

  override def postStop {
    log.info("street %s STOP" format (name))
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
  val discarding = new Stage {
    def name = "discarding"
    def run(context: Stage.Context) = {
    }
  }

  val bringIn = new Stage {
    def name = "bring-in"
    def run(context: Stage.Context) = {
      context.gameplay.bringIn
    }
  }

  val bigBets = new Stage {
    def name = "big-bets"
    def run(context: Stage.Context) = {
      context.gameplay.betting ! Betting.BigBets
    }
  }

  val betting = new Skippable {
    def name = "betting"
    def run(context: Stage.Context) = {
      context.gameplay.betting ! Betting.Start
    }
  }

  def dealing(dealType: Dealer.DealType, cardsNum: Option[Int] = None): Stage = new Stage {
    def name = "dealing"
    def run(context: Stage.Context) = {
      context.gameplay.dealCards(dealType, cardsNum)
    }
  }

  final val ByGameGroup: Map[Game.Group, List[Street]] = Map(
    Game.Holdem -> List(
      Street.Preflop(
        List(
          dealing(Dealer.Hole),
          betting)),
      Street.Flop(
        List(
          dealing(Dealer.Board, Some(3)),
          betting)),
      Street.Turn(
        List(
          dealing(Dealer.Board, Some(1)),
          bigBets,
          betting)),
      Street.River(
        List(
          dealing(Dealer.Board, Some(1)),
          betting))),

    Game.SevenCard -> List(
      Street.Second(
        List(
          dealing(Dealer.Hole, Some(2)))),
      Street.Third(
        List(
          dealing(Dealer.Door, Some(1)),
          bringIn,
          betting)),
      Street.Fourth(
        List(
          dealing(Dealer.Door, Some(1)),
          betting)),
      Street.Fifth(
        List(
          dealing(Dealer.Door, Some(1)),
          bigBets,
          betting)),
      Street.Sixth(
        List(
          dealing(Dealer.Door, Some(1)),
          betting)),
      Street.Seventh(
        List(
          dealing(Dealer.Hole, Some(1)),
          betting))),

    Game.SingleDraw -> List(
      Street.Predraw(
        List(
          dealing(Dealer.Hole, Some(5)),
          betting,
          discarding)),
      Street.Draw(
        List(
          bigBets,
          betting,
          discarding))),

    Game.TripleDraw -> List(
      Street.Predraw(
        List(
          dealing(Dealer.Hole),
          betting,
          discarding)),
      Street.FirstDraw(
        List(
          betting,
          discarding)),
      Street.SecondDraw(
        List(
          bigBets,
          betting,
          discarding)),
      Street.ThirdDraw(
        List(
          betting,
          discarding))))
}
