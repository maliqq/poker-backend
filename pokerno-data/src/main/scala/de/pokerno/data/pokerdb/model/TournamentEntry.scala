package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

object TournamentEntry {
  import de.pokerno.data.pokerdb.PokerDB._
  
  def create(tournamentId: UUID, playerId: UUID) = {
    tournamentEntries.insert(TournamentEntry(tournamentId, playerId))
  }
}

sealed case class TournamentEntry(
  @Column("tournament_id") tournamentId: UUID,
  @Column("player_id") playerId: UUID,
  @Column("rebuys_count") rebuysCount: Int = 0,
  addon: Boolean = false,
  @Column("knockouts_count") knockoutsCount: Int = 0,
  @Column(optionType = classOf[Int]) place: Option[Int] = None,
  @Column(optionType = classOf[Double]) payout: Option[Double] = None
  ) {
  var id: Long = 0
  def this() = this(null, null)
  
  import de.pokerno.data.pokerdb.PokerDB._
  
  def incrementRebuysCount() {
    update(tournamentEntries)((e) =>
      where(e.id === id)
      set(e.rebuysCount := e.rebuysCount plus 1)
    )
  }
  
  def markAddon() {
    update(tournamentEntries)((e) =>
      where(e.id === id)
      set(e.addon := true)
    )
  } 
  
  def incrementKnockoutsCount() {
    update(tournamentEntries)((e) =>
      where(e.id === id)
      set(e.knockoutsCount := e.knockoutsCount plus 1)
    )
  }
  
}
