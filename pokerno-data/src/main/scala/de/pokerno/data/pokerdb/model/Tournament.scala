package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

object Tournament {
  import de.pokerno.data.pokerdb.PokerDB._
  
  def getBuyIn(tournamentId: UUID): TournamentBuyIn = {
    join(tournaments, tournamentBuyIns)((tournament, tournamentBuyIn) =>
      where(tournament.id === tournamentId)
      select(tournamentBuyIn)
      on(tournament.buyInId === tournamentBuyIn.id)
    ).head
  }
  
}

sealed case class Tournament(
    @Column(name = "game_id", optionType = classOf[Int]) var gameId: Option[Long],
    @Column(name = "mix_id", optionType = classOf[Int]) var mixId: Option[Long],
    @Column("buy_in_id") var buyInId: Long
) extends KeyedEntity[UUID]{
  var id: UUID = null
  def this() = this(None, None, 0)
}
