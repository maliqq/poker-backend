package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

sealed case class TournamentPlayingFormat(
  @Column("freezout") isFreeout: Boolean = false,
  @Column("knockout") isKnockout: Boolean = false,
  @Column("shootout") isShootout: Boolean = false,
  @Column("rebuys") hasRebuys: Boolean = false.
  @Column("addons") hasAddons: Boolean = false
  @Column(name = "rebuys_limit", optionType = classOf[Int]) maxRebuys: Option[Int] = None
  ) {
  var id: Long = 0
}
