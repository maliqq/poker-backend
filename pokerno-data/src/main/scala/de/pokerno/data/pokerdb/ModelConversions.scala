package de.pokerno.data.pokerdb

import de.pokerno.model

object ModelConversions {

  implicit def game2model(g: PokerDB.Game): model.Game = {
    new model.Game(
        g.variation: model.GameType,
        g.limit.get: model.GameLimit,
        g.tableSize
      )
  }

  implicit def stake2model(g: PokerDB.Stake): model.Stake = {
    new model.Stake(
        g.bigBlind,
        g.smallBlind,
        (g.buyInMin, g.buyInMax),
        g.ante.map(math.BigDecimal(_)),
        None
      )
  }
  
}
