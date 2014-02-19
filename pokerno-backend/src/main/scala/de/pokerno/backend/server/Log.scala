package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging}

class Log extends Actor with ActorLogging {
  
  override def preStart() {
  }
  
  def receive = {
    case _ =>
  }
  
  override def postStop() {
  }
  
}
