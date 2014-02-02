package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

object Streets {
  case object Next
  case object Done
  
  import Stages._
  
  trait DealContext {
    deal: Deal =>

    def streets: StreetChain
    
    lazy val beforeStreets =
      stage("prepare-seats") { ctx =>
        ctx.gameplay.prepareSeats(ctx)
        Stage.Next
  
      } chain stage("rotate-game") { ctx =>
        ctx.gameplay.rotateGame(ctx)
        Stage.Next
  
      } chain stage("post-antes") { ctx =>
        ctx.gameplay.postAntes(ctx)
        Stage.Next
  
      } chain stage("post-blinds") { ctx =>
        ctx.gameplay.postBlinds(ctx)
        Stage.Next
      }
    
    lazy val afterStreets =
      stage("showdown") { ctx =>
        ctx.gameplay.showdown()
        Stage.Next
      }.chain
      
    def handleStreets: Receive = {
      case Betting.Start =>
        log.info("[betting] start")
        // FIXME
        //gameplay.round.reset()
        nextTurn()//.foreach(self ! _)
        context.become(handleBetting)
      
      case Streets.Next ⇒
        log.info("streets next")
        streets(stageContext)
  
      case Streets.Done ⇒
        log.info("streets done")
        afterStreets(stageContext)
        context.stop(self)
    }
  }
  
  trait ReplayContext {
    replay: Replay =>
  }
  
  def apply(ctx: StageContext) = {
    val gameOptions = ctx.gameplay.game.options
    val streetOptions = Options.byGameGroup(gameOptions.group)
    new StreetChain(ctx, streetOptions)
  }
  
  object Options {
    import Street._
    
    def apply(gameGroup: Game.Group) = byGameGroup(gameGroup)
    
    implicit def dealingCards2dealingOptions(v: DealCards.Value): DealingOptions = DealingOptions(v)
    implicit def dealingOptions2option(v: DealCards.Value): Option[DealingOptions] = Some(v)
    implicit def dealingOptions2option(v: DealingOptions): Option[DealingOptions] = Some(v)
    
    final val byGameGroup = Map[Game.Group, Map[Street.Value, StreetOptions]](
        Game.Holdem -> Map(
            Preflop -> StreetOptions(
                dealing = DealCards.Hole,
                betting = true),
        
            Flop -> StreetOptions(
                dealing = DealCards.Board(3),
                betting = true),
    
            Turn -> StreetOptions(
                dealing = DealCards.Board(1),
                bigBets = true,
                betting = true),
          
            River -> StreetOptions(
                dealing = DealCards.Board(1),
                betting = true)
        ),
        
        Game.SevenCard -> Map(
            Second -> StreetOptions(
                dealing = DealCards.Hole(2)
            ),
          
            Third -> StreetOptions(
                dealing = DealCards.Door(1),
                bringIn = true,
                betting = true),
            
            Fourth -> StreetOptions(
                dealing = DealCards.Door(1),
                betting = true),
            
            Fifth -> StreetOptions(
                dealing = DealCards.Door(1),
                bigBets = true,
                betting = true),
          
            Sixth -> StreetOptions(
                dealing = DealCards.Door(1),
                betting = true),
            
            Seventh -> StreetOptions(
                dealing = DealCards.Hole(1),
                betting = true)
        ),
        
        Game.SingleDraw -> Map(
            Predraw -> StreetOptions(
                dealing = DealCards.Hole(5),
                betting = true,
                discarding = true),
            
            Draw -> StreetOptions(
                bigBets = true,
                betting = true,
                discarding = true)
        ),
        
        Game.TripleDraw -> Map(
            Predraw -> StreetOptions(
                dealing = DealCards.Hole,
                betting = true,
                discarding = true),
            
            FirstDraw -> StreetOptions(
                betting = true,
                discarding = true),
            
            SecondDraw -> StreetOptions(
                bigBets = true,
                betting = true,
                discarding = true),
            
            ThirdDraw -> StreetOptions(
                betting = true,
                discarding = true)
        )
    )
  }
  
  
}
