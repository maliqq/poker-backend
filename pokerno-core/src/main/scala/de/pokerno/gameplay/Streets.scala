package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

class Streets(chain: Function1[StageContext, StageContext]) {
  def next
  def stop
}

object Streets extends Stages {
  
  val bringIn = stage("bring-in") { ctx =>
    ctx.gameplay.bringIn(ctx)
  }
  
  val discarding = stage("discarding") { ctx =>
    Console printf("not implemented")
  }
  
  val bigBets = stage("big-bets") { ctx =>
    ctx.ref ! Betting.BigBets
  }
  
  val betting = stage("betting") { ctx =>
    ctx.ref ! Betting.Start
  }
  
  implicit def int2option(n: Int): Option[Int] = Some(n)
  
  def dealing(dealType: DealCards.Value, cardsNum: Option[Int] = None) =
    stage("dealing") { ctx =>
      ctx.gameplay.dealCards(dealType, cardsNum)
    }
  
  def apply(ctx: StageContext) = {
    val gameGroup = ctx.gameplay.game.options.group
    
    val b = new Builder
    gameGroup match {
      case Game.Holdem =>
        b.holdem(ctx)
      
      case Game.SevenCard =>
        b.sevenCard(ctx)
      
      case Game.SingleDraw =>
        b.singleDraw(ctx)
      
      case Game.TripleDraw =>
        b.tripleDraw(ctx)
    }
    
    b.build
  }
  
  class Builder {
    import Street._
    
    private var streets: StreetChain = null
    
    private def street(value: Street.Value)(u: => StageChain) {
      if (streets == null) streets = new StreetChain(u)
      else streets = streets chain u
    }
    
    def build = streets
    
    def holdem(ctx: StageContext) {
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
    
    def sevenCard(ctx: StageContext) {
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
    
    def singleDraw(ctx: StageContext) {
      street(Predraw) {
        dealing(DealCards.Hole, 5) chain betting chain discarding
      }
  
      street(Draw) {
        bigBets chain betting chain discarding
      }
    }
    
    def tripleDraw(ctx: StageContext) {
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
