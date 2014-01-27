package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

object Streets {
  case object Next
  case object Done
  
  import Stages._
  
  val bringIn = stage("bring-in") { ctx =>
    ctx.gameplay.bringIn(ctx)
    Stage.Next
  }
  
  val discarding = stage("discarding") { ctx => Stage.Next }
  
  val bigBets = stage("big-bets") { ctx =>
    ctx.ref ! Betting.BigBets
    Stage.Next
  }
  
  val betting = stage("betting") { ctx =>
    ctx.ref ! Betting.Start
    Stage.Wait
  }
  
  implicit def int2option(n: Int): Option[Int] = Some(n)
  
  def dealing(dealType: DealCards.Value, cardsNum: Option[Int] = None) =
    stage("dealing") { ctx =>
      ctx.gameplay.dealCards(dealType, cardsNum)
      Stage.Next
    }
  
  def apply(ctx: StageContext) = {
    val gameGroup = ctx.gameplay.game.options.group
    
    val b = new Builder(ctx)
    gameGroup match {
      case Game.Holdem =>
        b.holdem
      
      case Game.SevenCard =>
        b.sevenCard
      
      case Game.SingleDraw =>
        b.singleDraw
      
      case Game.TripleDraw =>
        b.tripleDraw
      
      case _ => throw new IllegalArgumentException("unknown game group {}" format(gameGroup))
    }
    
    b.build
  }
  
  class Builder(ctx: StageContext) {
    import Street._
    
    private var result: List[Street] = List()
    
    private def street(value: Street.Value)(u: => StageChain) {
      result :+= Street(value, u)
    }
    
    def build = new StreetChain(ctx, result)
    
    def holdem {
      street(Preflop) {
        dealing(DealCards.Hole) chain betting
      }
      
      street(Flop) {
        dealing(DealCards.Board, 3) chain betting
      }
  
      street(Turn) {
        dealing(DealCards.Board, 1) chain bigBets chain betting
      }
    
      street(River) {
        dealing(DealCards.Board, 1) chain betting
      }
    }
    
    def sevenCard {
      street(Second) {
        dealing(DealCards.Hole, 2) chain
      }
    
      street(Third) {
        dealing(DealCards.Door, 1) chain bringIn chain betting
      }
    
      street(Fourth) {
        dealing(DealCards.Door, 1) chain betting
      }
    
      street(Fifth) {
        dealing(DealCards.Door, 1) chain bigBets chain betting
      }
    
      street(Sixth) {
        dealing(DealCards.Door, 1) chain betting
      }
    
      street(Seventh) {
        dealing(DealCards.Hole, 1) chain betting
      }
    }
    
    def singleDraw {
      street(Predraw) {
        dealing(DealCards.Hole, 5) chain betting chain discarding
      }
  
      street(Draw) {
        bigBets chain betting chain discarding
      }
    }
    
    def tripleDraw {
      street(Predraw) {
        dealing(DealCards.Hole) chain betting chain discarding
      }
    
      street(FirstDraw) {
        betting chain discarding
      }
      
      street(SecondDraw) {
        bigBets chain betting chain discarding
      }
    
      street(ThirdDraw) {
        betting chain discarding
      }
    }
  }
}
