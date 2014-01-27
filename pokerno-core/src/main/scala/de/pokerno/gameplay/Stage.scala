package de.pokerno.gameplay

import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object Stages {
  type Stage = Function1[GameplayContext, GameplayContext]
}

trait Stages {
  import Stages._
  
  def stage(name: String)(u: GameplayContext => Unit): Stage = {
    new PartialFunction[GameplayContext, GameplayContext] {
      def apply(g: GameplayContext): GameplayContext = {
        Console printf ("%s*** START %s%s\n", Console.BLUE, name, Console.RESET)
        u(g)
        Console printf ("%s*** DONE %s%s\n", Console.BLUE, name, Console.RESET)
        g
      }
    }
  }
}
