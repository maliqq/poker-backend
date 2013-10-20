package pokerno.backend.engine

import scala.math.{BigDecimal => Decimal}
import akka.actor.{Actor}
import com.codahale.metrics._

object Metrics {

}

class Metrics extends Actor {
  final val metrics = new MetricRegistry
  val deals = metrics.meter("deals")
  
  override def preStart = {
    
  }
  
  case object Deal
  def receive = {
    case Deal => deals.mark
  }
}
