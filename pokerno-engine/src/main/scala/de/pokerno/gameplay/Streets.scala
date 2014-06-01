package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }

class StreetStageChain(val street: Street.Value) extends StageChain {
  override def toString = f"street:${street}"
}

case class Streets(ctx: StageContext, stages: Seq[StreetStageChain]) extends Stage {
  
  import ctx.gameplay._
  
  private val iterator = stages.iterator

  def apply() = if (iterator.hasNext) {
    val stage = iterator.next()

    ctx.street = Some(stage.street)
    ctx broadcast Events.streetStart(stage.street)

    stage(ctx) match {
      case Stage.Next | Stage.Skip ⇒
        ctx.ref ! Streets.Next

      case Stage.Wait ⇒
        println("waiting")

      case x ⇒
        throw new MatchError("unhandled stage transition: %s".format(x))
    }

  } else ctx.ref ! Streets.Done
}

private[gameplay] object Streets {
  case object Next
  case object Done
  
  trait Default {
    import Stages._
    import de.pokerno.gameplay.stage.{ PostBlinds,  RotateGame, PostAntes, PrepareSeats, Showdown, PlayStart, PlayStop }
    
    lazy val beforeStreets =
      stage[PlayStart]    ("play-start") ~>
      stage[PrepareSeats] ("prepare-seats") ~>
      stage[RotateGame]   ("rotate-game") ~>
      stage[PostAntes]    ("post-antes") ~>
      stage[PostBlinds]   ("post-blinds")

    lazy val afterStreets =
      stage[Showdown]     ("showdown") ~>
      stage[PlayStop]     ("play-stop")
  }

  def apply(ctx: StageContext): Streets = {
    val gameOptions = ctx.gameplay.game.options

    val streets = Street.byGameGroup(gameOptions.group)
    val stages = streets.map { street =>
      new StageBuilder(street, streetOptions(street)).build()
    }

    Streets(ctx, stages)
  }

  case class StreetOptions(
    dealing: Option[Tuple2[DealType.Value, Option[Int]]] = None,
    bringIn: Boolean = false,
    bigBets: Boolean = false,
    betting: Boolean = false,
    discarding: Boolean = false
  )

  class StageBuilder(
      street: Street.Value,
      dealing: Option[Tuple2[DealType.Value, Option[Int]]],
      bringIn: Boolean,
      bigBets: Boolean,
      betting: Boolean,
      discarding: Boolean
    ) {

    def this(street: Street.Value, opts: StreetOptions) = this(street,
        dealing     = opts.dealing,
        bringIn     = opts.bringIn,
        bigBets     = opts.bigBets,
        betting     = opts.betting,
        discarding  = opts.discarding
      )
      
    import Stages.process
    import stage.{Dealing}

    def build(): StreetStageChain = {
      val stages = new StreetStageChain(street)

      dealing.map { case (dealType, cardsNum) ⇒
        stages ~> process("dealing") { ctx ⇒
          Dealing(ctx, dealType, cardsNum).apply()
        }
      }

      if (bigBets) {
        stages ~> process("big-bets") { ctx ⇒
          ctx.ref ! Betting.BigBets
        }
      }

      if (betting) {
        stages ~> process("betting", Stage.Wait) { ctx ⇒
          ctx.ref ! Betting.Start
        }
      }

      if (discarding) {
        stages ~> process("discarding") { ctx ⇒  }
      }

      stages
    }

    override def toString = f"#[Street ${street}]"
  }

  import Street._
  final val streetOptions = Map[Street.Value, StreetOptions](
    Preflop -> StreetOptions(
      dealing = Some((DealType.Hole, None)),
      betting = true),

    Flop -> StreetOptions(
      dealing = Some((DealType.Board, Some(3))),
      betting = true),

    Turn -> StreetOptions(
      dealing = Some((DealType.Board, Some(1))),
      bigBets = true,
      betting = true),

    River -> StreetOptions(
      dealing = Some((DealType.Board, Some(1))),
      betting = true),

    Second -> StreetOptions(
      dealing = Some((DealType.Hole, Some(2)))
    ),

    Third -> StreetOptions(
      dealing = Some((DealType.Door, Some(1))),
      bringIn = true,
      betting = true),

    Fourth -> StreetOptions(
      dealing = Some((DealType.Door, Some(1))),
      betting = true),

    Fifth -> StreetOptions(
      dealing = Some((DealType.Door, Some(1))),
      bigBets = true,
      betting = true),

    Sixth -> StreetOptions(
      dealing = Some((DealType.Door, Some(1))),
      betting = true),

    Seventh -> StreetOptions(
        dealing = Some((DealType.Hole, Some(1))),
        betting = true),

    Predraw -> StreetOptions(
      dealing = Some((DealType.Hole, Some(5))),
      betting = true,
      discarding = true),

    Draw -> StreetOptions(
      bigBets = true,
      betting = true,
      discarding = true),

    Predraw -> StreetOptions(
      dealing = Some((DealType.Hole, None)),
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

}
