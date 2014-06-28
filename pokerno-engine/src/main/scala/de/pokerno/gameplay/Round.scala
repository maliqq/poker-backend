package de.pokerno.gameplay

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.annotation.{JsonProperty, JsonIgnore, JsonInclude, JsonGetter}
import com.fasterxml.jackson.databind.annotation.JsonSerialize

import de.pokerno.poker.Cards
import de.pokerno.model.{Table, Game, Player, Dealer}
import de.pokerno.model.table.seat.{Sitting, Acting}

private[gameplay] class Sitting2Acting extends com.fasterxml.jackson.databind.util.StdConverter[Option[Sitting], Option[Acting]] {
  override def convert(seat: Option[Sitting]): Option[Acting] = seat.map(_.asActing)
}

private[gameplay] object Round {

  trait Transition
  
  // require action from this position
  case class Require(seat: Sitting) extends Transition

  // stop current deal
  case object Stop extends Transition

  // betting done - wait for next street to occur
  case object Done extends Transition
  
  // action timeout - go to next seat
  case object Timeout
  
  // action timer pause/resume
  case object Pause
  case object Resume

}

private[gameplay] abstract class Round(table: Table) {
  
  private val log = LoggerFactory.getLogger(getClass)
  
  // SEATS
  private var _seats = table.sittingFromButton
  def seats = _seats
  
  // ACTING
  private var _acting: Option[Sitting] = None
  @JsonSerialize(converter = classOf[Sitting2Acting])  def acting = _acting
  protected def acting_=(seat: Sitting) {
    _acting = Some(seat)
    _seats = table.sittingFrom(seat.pos)
  }
  
  @JsonGetter def current = _acting map(_.pos)
  
  def reset() {
    _acting = None
    _seats = table.sittingFromButton
  }
  
}
