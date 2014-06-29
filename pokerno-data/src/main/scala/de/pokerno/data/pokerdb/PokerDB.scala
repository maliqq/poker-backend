package de.pokerno.data.pokerdb

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

import model._

object PokerDB extends Schema {
  
  val nodes     = table[Node]("nodes")
  val rooms     = table[Room]("poker_rooms")
  val sessions  = table[PlaySession]("poker_play_sessions")
  val games     = table[Game]("poker_variations")
  val mixes     = table[Mix]("poker_variations")
  val stakes    = table[Stake]("poker_stakes")
  
  val tournaments       = table[Tournament]("poker_tournaments")
  val tournamentBuyIns  = table[TournamentBuyIn]("poker_tournament_buy_ins")
  val tournamentEntries = table[TournamentEntry]("poker_tournament_entries")
  
  lazy val roomsWithGamesAndStakes = join(rooms, games.leftOuter, mixes.leftOuter, stakes)((room, game, mix, stake) =>
    select((room, game, mix, stake))
    on(room.gameId === game.map(_.id), room.mixId === mix.map(_.id), room.stakeId === stake.id)
  )
  
}
