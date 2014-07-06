package de.pokerno.gameplay.tournament

object Bubble {
  case object Pause
  case object Resume
}

trait Bubble {
  
  var bubbleEnded: Option[java.time.Instant]
  def endBubble() = bubbleEnded = Some(java.time.Instant.now())
  def passedBubble() = bubbleEnded.isDefined
  
}
