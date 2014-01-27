package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.{ Actor, Props, ActorLogging, ActorRef }
import de.pokerno.protocol.{msg => proto}

object Street extends Enumeration {
    
  case object Start
  case object Next
  case object Exit
  
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

class StreetChain(u: => StageChain) {
  def chain(u: => StageChain): StreetChain = {
    this
  }
  
  def apply(ctx: StageContext) {
    
  }
}
