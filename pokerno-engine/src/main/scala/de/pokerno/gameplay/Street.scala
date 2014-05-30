package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.GameEvent

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

  var stages = new StageChain

  options.dealing.map { o ⇒
    val dealing = stage("dealing") { ctx ⇒
      Dealing(ctx, o._1, o._2).process()
      Stage.Next
    }
    stages ~> dealing
  }

  if (options.bringIn) {
    val bringIn = stage("bring-in") { ctx ⇒
      BringIn(ctx).process()
      Stage.Next
    }
    stages ~> bringIn
  }

  if (options.bigBets) {
    val bigBets = stage("big-bets") { ctx ⇒
      ctx.ref ! Betting.BigBets
      Stage.Next
    }
    stages ~> bigBets
  }

  if (options.betting) {
    val betting = stage("betting") { ctx ⇒
      ctx.ref ! Betting.Start
      Stage.Wait
    }
    stages ~> betting
  }

  if (options.discarding) {
    val discarding = stage("discarding") { ctx ⇒ Stage.Next }
    stages ~> discarding
  }

  def apply(ctx: StageContext) = stages(ctx)

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

private[gameplay] class StreetChain(
    ctx: StageContext,
    streetOptions: Map[Street.Value, StreetOptions]) extends Stage(ctx) {
  
  private val streets = Street.byGameGroup(game.options.group)
  private val iterator = streets.iterator
  private var _current: Option[Street.Value] = None

  def current = _current

  def process() = if (iterator.hasNext) {
    val street = iterator.next()
    val stage = new StreetStage(street, streetOptions(street))
    _current = Some(street)

    ctx.broadcast(
        GameEvent.streetStart(street))

    stage(ctx) match {
      case Stage.Next | Stage.Skip ⇒
        ctx.ref ! Streets.Next

      case Stage.Wait ⇒
        println("waiting")

      case x ⇒
        throw new MatchError("unhandled stage transition: %s".format(x))
    }

  } else ctx.ref ! Streets.Done

  override def toString = {
    val b = new StringBuilder
    b.append("#[StreetChain")
    for (streetOption ← streetOptions) {
      b.append(" " + streetOption.toString)
    }
    b.append("]").toString()
  }
}
