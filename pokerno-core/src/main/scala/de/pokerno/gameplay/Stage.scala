package de.pokerno.gameplay

import akka.actor.ActorRef

trait Stages {
  def stage(name: String)(f: StageContext => Unit): Stage = {
    new Stage(name, f)
  }
}

case class StageContext(gameplay: GameplayContext, ref: ActorRef)

class Stage(name: String, f: StageContext => Unit) {
  val context: StageContext = null
  
  def chain(f: => Stage): StageChain = {
    (new StageChain(this)) chain (f)
  }
  
  def chain = {
    new StageChain(this)
  }
}

class StageChain(stage: Stage) {
  
  def chain(f: => Stage): StageChain = {
    this
  }
  
  def apply(ctx: StageContext) {
    
  }
  
}
