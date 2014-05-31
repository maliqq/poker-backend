package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }

object Street extends Enumeration {
  private def value(name: String) = new Val(nextId, name)

  // holdem poker
  val Preflop     = value("preflop")
  val Flop        = value("flop")
  val Turn        = value("turn")
  val River       = value("river")

  // seven card stud
  val Second      = value("second")
  val Third       = value("third")
  val Fourth      = value("fourth")
  val Fifth       = value("fifth")
  val Sixth       = value("sixth")
  val Seventh     = value("seventh")

  // draw poker
  val Predraw     = value("predraw")
  val Draw        = value("draw")
  val FirstDraw   = value("first-draw")
  val SecondDraw  = value("second-draw")
  val ThirdDraw   = value("third-draw")

  implicit def string2streetValueOption(s: String): Option[Value] = values.find(_.toString == s)

  final val byGameGroup = Map[Game.Group, Seq[Value]](
    Game.Holdem ->
      Seq(Preflop, Flop, Turn, River),
    Game.SevenCard ->
      Seq(Second, Third, Fourth, Fifth, Sixth, Seventh),
    Game.SingleDraw ->
      Seq(Predraw, Draw),
    Game.TripleDraw ->
      Seq(Predraw, FirstDraw, SecondDraw, ThirdDraw)
  )
}

private[gameplay] object Chain {
  trait Result
  case object Next extends Result
  case object Stop extends Result
}

private[gameplay] case class StreetStage(street: Street.Value, options: StreetOptions) {
  
  import Stages._
  import de.pokerno.gameplay.stage.{BringIn, Dealing}

  var stages = new StageChain

  options.dealing.map { o ⇒
    stages ~> process("dealing") { ctx ⇒
      Dealing(ctx, o._1, o._2).apply()
    }
  }

  if (options.bringIn)
    stages ~> stage[BringIn]("bring-in")

  if (options.bigBets)
    stages ~> process("big-bets") { ctx ⇒
      ctx.ref ! Betting.BigBets
    }

  if (options.betting)
    stages ~> process("betting", Stage.Wait) { ctx ⇒
      ctx.ref ! Betting.Start
    }

  if (options.discarding)
    stages ~> process("discarding") { ctx ⇒  }

  def apply(ctx: StageContext) = stages.apply(ctx)

  override def toString = f"#[Street ${street}]"
}


private[gameplay] case class StreetOptions(
  dealing: Option[Tuple2[DealType.Value, Option[Int]]] = None,
  bringIn: Boolean = false,
  bigBets: Boolean = false,
  betting: Boolean = false,
  discarding: Boolean = false
) {
  override def toString = {
    val b = new StringBuilder
    
    dealing.map { d =>
                    b.append(" dealing=%s" format d)
    }
    if (bringIn)    b.append(" ✓bring-in")
    if (bigBets)    b.append(" ✓big-bets")
    if (betting)    b.append(" ✓betting")
    if (discarding) b.append(" ✓discarding")
    
    b.toString
  }
}

