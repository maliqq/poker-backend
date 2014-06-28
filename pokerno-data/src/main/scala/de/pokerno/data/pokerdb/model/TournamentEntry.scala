package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

sealed case class TournamentEntry(
  @Column("tournament_id") tournamentId: UUID,
  @Column("player_id") playerId: UUID,
  @Column(name = "rebuys_count", optionType = classOf[Int]) rebuysCount: Option[Int],
  @Column(optionType = classOf[Boolean]) addon: Option[Boolean],
  @Column(name = "knockouts_count", optionType = classOf[Int]) knockoutsCount: Option[Int],
  @Column(optionType = classOf[Int]) place: Option[Int],
  @Column(optionType = classOf[Double]) payout: Option[Double]
  ) {
  var id: Long = 0
  def this() = this(null, null, None, None, None, None, None)
}
