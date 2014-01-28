package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

object Street extends Enumeration {
    
  class StreetVal(i: Int, name: String) extends Val(i, name)
  
  private def value(name: String) = new StreetVal(nextId, name)
  
  val Preflop = value("preflop")
  val Flop = value("flop")
  val Turn = value("turn")
  val River = value("river")
 
  val Second = value("second")
  val Third = value("third")
  val Fourth = value("fourth")
  val Fifth = value("fifth")
  val Sixth = value("sixth")
  val Seventh = value("seventh")
 
  val Predraw = value("predraw")
  val Draw = value("draw")
  val FirstDraw = value("first-draw")
  val SecondDraw = value("second-draw")
  val ThirdDraw = value("third-draw")
  
}

object Chain {
  trait Result
  case object Next extends Result
  case object Stop extends Result
}

case class Street(value: Street.Value, stages: StageChain) {
  def apply(ctx: StageContext) = stages(ctx)
  
  override def toString = f"#[Street $value]"
}

class StreetChain(ctx: StageContext, streets: List[Street]) {
  private val iterator = streets.iterator
  private var _current: Street = null
  
  def current = _current
  
  def apply(ctx: StageContext) = if (iterator.hasNext) {
    _current = iterator.next()
    
    current(ctx) match {
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
    for (street <- streets) {
      b.append(" " + street.toString)
    }
    b.append("]").toString()
  }
}
