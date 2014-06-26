package de.pokerno.gameplay.tournament

trait Elimination {
  
  val ctx: Context
  
  import ctx._
  
  def eliminate(player: Player, placeNr: Int) {
    balance.award(player, ctx.id.toString(), placeNr)
  }
  
}
