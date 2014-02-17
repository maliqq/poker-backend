package de.pokerno.backend.storage

import akka.actor.{ Actor, ActorLogging }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.poker
import de.pokerno.protocol.msg

object PlayHistory {

  class Entry(
    val id: String,
    val startAt: java.util.Date,
    val stopAt: java.util.Date,
    val pot: Decimal,
    val rake: Option[Decimal] = None,
    val actions: List[msg.Message],
    val winners: Map[String, Decimal],
    val knownCards: Map[String, List[poker.Card]])

  class Recorder(client: Store.Client) extends Actor {
    def receive = {
      case e: PlayHistory.Entry ⇒
        client.write(e)
    }
  }

}
