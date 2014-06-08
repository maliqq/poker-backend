package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }

class StreetStages[T <: stg.Context](val street: Street.Value, val stages: stg.Chain[T]) {
  def apply(ctx: T) = stages(ctx)
  override def toString = f"street:${street}"
}

case class Streets(ctx: stg.Context, stages: Seq[StreetStages[stg.Context]]) extends Stage {
  
  import ctx.gameplay._
  
  private val iterator = stages.iterator

  def apply() = if (iterator.hasNext) {
    val stage = iterator.next()

    play.street = stage.street
    events broadcast Events.streetStart(stage.street)

    stage(ctx) match {
      case Stage.Next | Stage.Skip ⇒
        ctx.ref ! Streets.Next

      case Stage.Wait ⇒
        // wait until betting completes

      case x ⇒
        throw new MatchError("unhandled stage transition: %s".format(x))
    }

  } else ctx.ref ! Streets.Done
}

object Streets {
  case object Next
  case object Done
  
  def apply(ctx: stg.Context): Streets = {
    import ctx.gameplay.gameOptions

    val stages = Street.byGameGroup(gameOptions.group).map(buildStages(_))

    Streets(ctx, stages)
  }

  case class StreetOptions(
    dealing: Option[Tuple2[DealType.Value, Option[Int]]] = None,
    bringIn: Boolean = false,
    bigBets: Boolean = false,
    betting: Boolean = false,
    discarding: Boolean = false
  )

  private def buildStages(street: Street.Value) = {
    def build(): StreetStages[stg.Context] = {
      import stages.Dealing

      val builder = new stg.Builder[stg.Context]()

      val options = streetOptions(street)
      options.dealing.map { case (dealType, cardsNum) ⇒
        builder.process("dealing") { ctx ⇒
          Dealing(ctx, dealType, cardsNum).apply()
        }
      }

      if (options.bigBets) {
        builder.process("big-bets") { ctx ⇒
          ctx.ref ! Betting.BigBets
        }
      }

      if (options.bringIn) {
        // TODO
      }

      if (options.betting) {
        builder.process("betting", Stage.Wait) { ctx ⇒
          ctx.ref ! Betting.Start
        }
      }

      if (options.discarding) {
        builder.process("discarding") { ctx ⇒  }
      }

      new StreetStages(street, builder.build())
    }

    build()
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
