package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.util.Colored._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }

private[gameplay] class StreetStages[T <: Stage.Context](val street: Street.Value, val stages: Stage.Chain[T]) {
  def apply(ctx: T) = stages(ctx)
  override def toString = f"street:${street}"
}

private[gameplay] case class Streets(ctx: Stage.Context, stages: Seq[StreetStages[Stage.Context]]) {
  
  import ctx.gameplay._
  
  private val iterator = stages.iterator
  private var _stage: StreetStages[Stage.Context] = null
  
  private def stage = _stage
  private def stage_=(stage: StreetStages[Stage.Context]) {
    _stage = stage
    info("=== %s ===", _stage.street)
    play.street = _stage.street
    events broadcast Events.streetStart(_stage.street)
  }
  
  def continue() = {
    _stage(ctx) match {
      case Stage.Next | Stage.Skip ⇒
        ctx.ref ! Streets.Next
  
      case Stage.Wait ⇒
        // wait until round completes
    }
  }

  def next() = {
    if (iterator.hasNext) {
      stage = iterator.next()
      continue()
    } else ctx.ref ! Streets.Done
  }
  
}

private[gameplay] object Streets {
  case object Next
  case object Continue
  case object Done
  
  def apply(ctx: Stage.Context): Streets = {
    import ctx.gameplay.gameOptions

    val stages = Street.byGameGroup(gameOptions.group).map(buildStages(_))

    Streets(ctx, stages)
  }

  case class StreetOptions(
    ante: Boolean = false,
    blinds: Boolean = false,
    dealing: Option[Tuple2[DealType.Value, Option[Int]]] = None,
    bringIn: Boolean = false,
    bigBets: Boolean = false,
    betting: Boolean = false,
    discarding: Boolean = false
  )

  private def buildStages(street: Street.Value) = {
    def build(): StreetStages[Stage.Context] = {
      import stage.impl.{Dealing, PostAntes, PostBlinds}

      val builder = new Stage.Builder[Stage.Context]()

      val options = streetOptions(street)
      
      if (options.ante) {
        builder.stage[PostAntes]("post-antes")
      }
      
      if (options.blinds) {
        builder.stage[PostBlinds]("post-blinds")
      }
      
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
        builder.process("discarding", Stage.Wait) { ctx ⇒
          ctx.ref ! Discarding.Start
        }
      }

      new StreetStages(street, builder.build())
    }

    build()
  }

  import Street._
  final val streetOptions = Map[Street.Value, StreetOptions](
    Preflop -> StreetOptions(
      ante = true,
      blinds = true,
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
      ante = true,
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
      ante = true,
      blinds = true,
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
