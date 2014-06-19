package de.pokerno.gameplay

import de.pokerno.model._
import akka.actor.ActorRef
import de.pokerno.protocol.GameEvent

private[gameplay] object Stages {
  
  trait Default {
    import Stages._
    import de.pokerno.gameplay.stages.{ PostBlinds,  RotateGame, PostAntes, Showdown, PlayStart, PlayStop }
    
    lazy val beforeStreets = new stg.Builder[stg.Context] {
      stage[PlayStart]    ("play-start")
      stage[RotateGame]   ("rotate-game")
      stage[PostAntes]    ("post-antes")
      stage[PostBlinds]   ("post-blinds")
    }.build()

    lazy val afterStreets = new stg.Builder[stg.Context] {
      stage[Showdown]     ("showdown")
      stage[PlayStop]     ("play-stop")
    }.build()
  }
  
}
