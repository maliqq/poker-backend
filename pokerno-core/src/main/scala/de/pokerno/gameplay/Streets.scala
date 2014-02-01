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
        gameplay.round.reset
        nextTurn()
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
    val streets = Mapping.byGameGroup(gameOptions.group)
    new StreetChain(ctx, streets)
  }
  
  object Mapping {
    import Street._
    
    private def street(value: Street.Value,
        dealing: Option[DealingOptions] = None,
        bringIn: Boolean = false,
        bigBets: Boolean = false,
        betting: Boolean = false,
        discarding: Boolean = false
        ) = {
      
      val options = StreetOptions(
          dealing = dealing,
          bringIn = bringIn,
          bigBets = bigBets,
          betting = betting,
          discarding = discarding)
    
      Street(value, options)
    }
    
    implicit def dealingCards2dealingOptions(v: DealCards.Value): DealingOptions = DealingOptions(v)
    implicit def dealingOptions2option(v: DealCards.Value): Option[DealingOptions] = Some(v)
    implicit def dealingOptions2option(v: DealingOptions): Option[DealingOptions] = Some(v)
    
    final val byGameGroup = Map[Game.Group, List[Street]](
        Game.Holdem -> List(
            street(Preflop,
                dealing = DealCards.Hole,
                betting = true),
        
            street(Flop,
                dealing = DealCards.Board(3),
                betting = true),
    
            street(Turn,
                dealing = DealCards.Board(1),
                bigBets = true,
                betting = true),
          
            street(River,
                dealing = DealCards.Board(1),
                betting = true)
        ),
        
        Game.SevenCard -> List(
            street(Second,
                dealing = DealCards.Hole(2)
            ),
          
            street(Third,
                dealing = DealCards.Door(1),
                bringIn = true,
                betting = true),
            
            street(Fourth,
                dealing = DealCards.Door(1),
                betting = true),
            
            street(Fifth,
                dealing = DealCards.Door(1),
                bigBets = true,
                betting = true),
          
            street(Sixth,
                dealing = DealCards.Door(1),
                betting = true),
            
            street(Seventh,
                dealing = DealCards.Hole(1),
                betting = true)
        ),
        
        Game.SingleDraw -> List(
            street(Predraw,
                dealing = DealCards.Hole(5),
                betting = true,
                discarding = true),
            
            street(Draw,
                bigBets = true,
                betting = true,
                discarding = true)
        ),
        
        Game.TripleDraw -> List(
            street(Predraw,
                dealing = DealCards.Hole,
                betting = true,
                discarding = true),
            
            street(FirstDraw,
                betting = true,
                discarding = true),
            
            street(SecondDraw,
                bigBets = true,
                betting = true,
                discarding = true),
            
            street(ThirdDraw,
                betting = true,
                discarding = true)
        )
    )
  }
  
  
}
