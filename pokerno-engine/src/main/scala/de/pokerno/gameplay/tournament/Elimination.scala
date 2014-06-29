package de.pokerno.gameplay.tournament

/*
Выбывание игрока из турнира
*/
trait Elimination {
  
  val ctx: Context
  def tournamentId = ctx.id.toString()
  
  import ctx._
  
  def eliminate(playerId: Player, placeNr: Int) {
    balance.award(tournamentId, playerId, placeNr)
  }
  
}
