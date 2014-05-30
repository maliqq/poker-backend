package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.GameEvent

private[gameplay] object Streets {
  case object Next
  case object Done

  import Stages._

  trait DealContext {
    deal: Deal ⇒

    lazy val beforeStreets =
      stage("play-start") { ctx ⇒
        ctx broadcast GameEvent.playStart(play)
        //play.started() // FIXME ugly
        Stage.Next
      } ~> stage("prepare-seats") { ctx ⇒
        ctx.gameplay.prepareSeats(ctx)

        if (ctx.gameplay.table.seats.count(_.canPlayNextDeal) <= 1) {
          // cancel current deal
          Stage.Exit
        } else Stage.Next

      } ~> stage("rotate-game") { ctx ⇒
        ctx.gameplay.rotateGame()
        Stage.Next

      } ~> stage("post-antes") { ctx ⇒
        PostAntes(ctx).process()
        Stage.Next

      } ~> stage("post-blinds") { ctx ⇒
        PostBlinds(ctx).process()
        Stage.Next
      }

    lazy val afterStreets =
      stage("showdown") { ctx ⇒
        Showdown(ctx).process()
        Stage.Next
      } ~> stage("play-stop") { ctx ⇒
        ctx.broadcast(
            GameEvent.playStop())
        play.finished()
        Stage.Next
      }

  }

  def apply(ctx: StageContext) = {
    val gameOptions = ctx.gameplay.game.options
    val streetOptions = Options.byGameGroup(gameOptions.group)
    new StreetChain(ctx, streetOptions)
  }

  object Options {
    import Street._

    def apply(gameGroup: Game.Group) = byGameGroup(gameGroup)

    final val byGameGroup = Map[Game.Group, Map[Street.Value, StreetOptions]](
      Game.Holdem -> Map(
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
          betting = true)
      ),

      Game.SevenCard -> Map(
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
          betting = true)
      ),

      Game.SingleDraw -> Map(
        Predraw -> StreetOptions(
          dealing = Some((DealType.Hole, Some(5))),
          betting = true,
          discarding = true),

        Draw -> StreetOptions(
          bigBets = true,
          betting = true,
          discarding = true)
      ),

      Game.TripleDraw -> Map(
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
    )
  }

}
