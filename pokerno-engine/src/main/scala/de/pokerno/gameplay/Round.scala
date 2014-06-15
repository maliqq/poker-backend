package de.pokerno.gameplay

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude, JsonGetter}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import math.{ BigDecimal ⇒ Decimal }

import de.pokerno.poker.Cards
import de.pokerno.model.{Table, Game, Player, Dealer, seat}

class Sitting2Acting extends com.fasterxml.jackson.databind.util.StdConverter[Option[seat.Sitting], Option[seat.Acting]] {
  override def convert(sitting: Option[seat.Sitting]): Option[seat.Acting] = sitting.map(_.asActing)
}

object Round {

  trait Transition
  
  // require action from this position
  case class Require(sitting: seat.Sitting) extends Transition

  // stop current deal
  case object Stop extends Transition

  // betting done - wait for next street to occur
  case object Done extends Transition
  
  // action timeout - go to next seat
  case object Timeout

}

abstract class Round(table: Table) {
  
  private val log = LoggerFactory.getLogger(getClass)
  
  // SEATS
  private var _seats = table.sittingFromButton
  def seats = _seats
  
  // ACTING
  private var _acting: Option[seat.Sitting] = None
  @JsonSerialize(converter = classOf[Sitting2Acting])  def acting = _acting
  protected def acting_=(sitting: seat.Sitting) {
    _acting = Some(sitting)
    _seats = table.sittingFrom(sitting.pos)
  }
  
  @JsonGetter def current = _acting map(_.pos)
  
  def reset() {
    _acting = None
    _seats = table.sittingFromButton
  }
  
}
