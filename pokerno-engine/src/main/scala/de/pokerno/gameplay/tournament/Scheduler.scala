package de.pokerno.gameplay.tournament

import akka.actor.{Actor, ActorLogging}
import concurrent.duration._

object Scheduler {
  trait Break { a => Actor
  }
  
  trait LevelUp { a => Actor
  }
  
  trait BubblePause { a => Actor
  }   
}

class Scheduler extends Actor {
  
  import context._
  import Scheduler._
  
  override def preStart {
    
  }
  
  def receive = {
    case _ =>
  }
  
  override def postStop {
    
  }
  
}
