package de.pokerno.form

import de.pokerno.model._

case class RoomInfo(
    variation: Variation,
    stake: Stake,
    table: Option[Table] = None
) {

}
