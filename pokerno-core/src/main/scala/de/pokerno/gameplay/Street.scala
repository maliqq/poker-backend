package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

class StreetActor(val gameplay: GameplayContext, val name: Street.Value, val stages: List[StreetStage]) extends Actor with ActorLogging {
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


object Streets extends Stages {
  
  val bringIn = stage("bring-in") { g => }
  val discarding = stage("discarding") { g => }
  val bigBets = stage("big-bets") { g => }
  val betting = stage("betting") { g => }
  
  implicit def int2option(n: Int): Option[Int] = Some(n)
  
  def dealing(dealType: DealCards.Value, cardsNum: Option[Int] = None) = stage("dealing") { g =>
    g.dealCards(dealType, cardsNum)
  }
  
  def apply(gameplay: GameplayContext) {
    val gameGroup = gameplay.game.options.group
    
    gameGroup match {
      case Game.Holdem => holdem(gameplay)
      case Game.SevenCard => sevenCard(gameplay)
      case Game.SingleDraw => singleDraw(gameplay)
      case Game.TripleDraw => tripleDraw(gameplay)
    }
  }
  
  def holdem(gameplay: GameplayContext) {
    Street.Preflop {
      dealing(DealCards.Hole) andThen betting
    }
    
    Street.Flop {
      dealing(DealCards.Board, Some(3)) andThen betting
    }

    Street.Turn {
      dealing(DealCards.Board, 1) andThen bigBets andThen betting
    }
  
    Street.River {
      dealing(DealCards.Board, 1) andThen betting
    }
  }
  
  def sevenCard(gameplay: GameplayContext) {
    Street.Second {
      dealing(DealCards.Hole, 2)
    }
  
    Street.Third {
      dealing(DealCards.Door, 1) andThen bringIn andThen betting
    }
  
    Street.Fourth {
      dealing(DealCards.Door, 1) andThen betting
    }
  
    Street.Fifth {
      dealing(DealCards.Door, 1) andThen bigBets andThen betting
    }
  
    Street.Sixth {
      dealing(DealCards.Door, 1) andThen betting
    }
  
    Street.Seventh {
      dealing(DealCards.Hole, 1) andThen betting
    }
  }
  
  def singleDraw(gameplay: GameplayContext) {
    Street.Predraw {
      dealing(DealCards.Hole, 5) andThen betting andThen discarding
    }

    Street.Draw {
      bigBets andThen betting andThen discarding
    }
  }
  
  def tripleDraw(gameplay: GameplayContext) {
    Street.Predraw {
      dealing(DealCards.Hole) andThen betting andThen discarding
    }
  
    Street.FirstDraw {
      betting andThen discarding
    }
  
    Street.SecondDraw{
      bigBets andThen betting andThen discarding
    }
  
    Street.ThirdDraw {
      betting andThen discarding
    }
  }
}
