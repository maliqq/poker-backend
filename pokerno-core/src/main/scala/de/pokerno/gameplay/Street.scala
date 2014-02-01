package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

object Street extends Enumeration {
  private def value(name: String) = new Val(nextId, name)
  
  // holdem poker
  val Preflop = value("preflop")
  val Flop = value("flop")
  val Turn = value("turn")
  val River = value("river")
 
  // seven card stud
  val Second = value("second")
  val Third = value("third")
  val Fourth = value("fourth")
  val Fifth = value("fifth")
  val Sixth = value("sixth")
  val Seventh = value("seventh")
 
  // draw poker
  val Predraw = value("predraw")
  val Draw = value("draw")
  val FirstDraw = value("first-draw")
  val SecondDraw = value("second-draw")
  val ThirdDraw = value("third-draw")
  
  implicit def string2streetValueOption(s: String): Option[Value] = values.find(_.toString == s)
  
  final val byGameGroup = Map[Game.Group, List[Value]](
      Game.Holdem ->
        List(Preflop, Flop, Turn, River),
      Game.SevenCard ->
        List(Second, Third, Fourth, Fifth, Sixth, Seventh),
      Game.SingleDraw ->
        List(Predraw, Draw),
      Game.TripleDraw ->
        List(Predraw, FirstDraw, SecondDraw, ThirdDraw)
  )
}

object Chain {
  trait Result
  case object Next extends Result
  case object Stop extends Result
}

case class Street(value: Street.Value, options: StreetOptions) {
  import Stages._
  
  var stages = new StageChain
  
  options.dealing.map { o =>
    val dealing = stage("dealing") { ctx =>
      ctx.gameplay.dealCards(o.dealType, o.cardsNum)
      Stage.Next
    }
    stages chain dealing
  }
  
  if (options.bringIn) {
    val bringIn = stage("bring-in") { ctx =>
      ctx.gameplay.bringIn(ctx)
      Stage.Next
    }
    stages chain bringIn
  }
  
  if (options.bigBets) {
    val bigBets = stage("big-bets") { ctx =>
      ctx.ref ! Betting.BigBets
      Stage.Next
    }
    stages chain bigBets
  }
  
  if (options.betting) {
    val betting = stage("betting") { ctx =>
      ctx.ref ! Betting.Start
      Stage.Wait
    }
    stages chain betting
  }
  
  if (options.discarding) {
    val discarding = stage("discarding") { ctx => Stage.Next }
    stages chain discarding
  }
  
  def apply(ctx: StageContext) = stages(ctx)
  
  override def toString = f"#[Street ${value}]"
}

case class StreetOptions(
    dealing: Option[DealingOptions] = None,
    bringIn: Boolean = false,
    betting: Boolean = false,
    bigBets: Boolean = false,
    discarding: Boolean = false
)

class StreetChain(
    ctx: StageContext,
    val streetOptions: Map[Street.Value, StreetOptions]) {
  private val streets = Street.byGameGroup(ctx.gameplay.game.options.group) 
  private val iterator = streets.iterator
  private var _current: Street = null
  
  def current = _current
  
  def apply(ctx: StageContext) = if (iterator.hasNext) {
    val _street = iterator.next()
    _current = new Street(_street, streetOptions(_street))
    
    _current(ctx) match {
      case Stage.Next | Stage.Skip =>
        ctx.ref ! Streets.Next
        
      case Stage.Wait =>
        println("waiting")
        
      case x => throw new MatchError("unhandled stage transition: %s".format(x))
    }
  
  } else ctx.ref ! Streets.Done
  
  override def toString = {
    val b = new StringBuilder
    b.append("#[StreetChain")
    for (streetOption <- streetOptions) {
      b.append(" " + streetOption.toString)
    }
    b.append("]").toString()
  }
}
