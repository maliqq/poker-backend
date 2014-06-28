package de.pokerno.data.pokerdb

import de.pokerno.model.{Game, GameType, GameLimit, Stake}

object ModelConversions {

  implicit def game2model(g: model.Game): Game = {
    new Game(
        g.variation: GameType,
        g.limit.get: GameLimit,
        g.tableSize
      )
  }

  implicit def stake2model(g: model.Stake): Stake = {
    new Stake(
        g.bigBlind,
        g.smallBlind,
        (g.buyInMin, g.buyInMax),
        g.ante.map(math.BigDecimal(_)),
        None
      )
  }
  
}
