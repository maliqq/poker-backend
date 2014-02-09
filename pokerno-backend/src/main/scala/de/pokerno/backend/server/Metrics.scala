package de.pokerno.backend.server

import akka.actor.{ Actor }
import com.codahale.metrics._

object Metrics {
}

class Metrics extends Actor {
  final val metrics = new MetricRegistry
  
  val deals = metrics.meter("deals")

  override def preStart() {
  }

  def receive = {
    case _ =>
  }
  
  override def postStop() {
    
  }
}
