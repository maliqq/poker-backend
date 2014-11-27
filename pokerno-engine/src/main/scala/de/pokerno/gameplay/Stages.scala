package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef
import de.pokerno.protocol.GameEvent

private[gameplay] object Stages {
  
  trait Default {
    import Stages._
    import de.pokerno.gameplay.stage.impl.{ PostBlinds,  RotateGame, PostAntes, Showdown, PlayStart, PlayStop }
    
    // TODO for {} yield
    lazy val beforeStreets = new stage.Builder[Stage.Context] {
      stage[PlayStart]    ("play-start")
      stage[RotateGame]   ("rotate-game")
    }.build()

    lazy val afterStreets = new stage.Builder[Stage.Context] {
      stage[Showdown]     ("showdown")
      stage[PlayStop]     ("play-stop")
    }.build()
  }
  
}
