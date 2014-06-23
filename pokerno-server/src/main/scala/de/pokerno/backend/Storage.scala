package de.pokerno.backend

import math.{BigDecimal => Decimal}
import de.pokerno.{model, poker}

abstract class PlayHistoryBatch {
  def writeEntry(
    roomId: java.util.UUID,
    started: java.util.Date,
    ended: java.util.Date,
    game: model.GameType,
    limit: model.GameLimit,
    stake: model.Stake,
    button: Int,            // staring position at the table
    board: poker.Cards,
    pot: Decimal,           // total size of the pot
    rake: Option[Decimal]        // rake
  )

  def writePosition(
    pos: Int,           // position at the table
    player: String,     // player id/uuid
    amount: Decimal,    // amount at start of the deal
    net: Decimal,       // amount won/lost
    cards: poker.Cards        // cards (if shown at showdown)
  )

  def writeAction(
    at: java.util.Date,     // event date
    player: String,         // player acted
    street: String,         // street
    bet: model.Bet                // card or chip actin
  )

  def write()
}

abstract class Storage {
  def write(roomId: java.util.UUID, game: model.Game, stake: model.Stake, play: model.Play)
}

sealed class DummyStorage extends Storage {
  def write(roomId: java.util.UUID, game: model.Game, stake: model.Stake, play: model.Play) {}
}

abstract class BatchedStorage extends Storage {
  protected def batch(id: java.util.UUID)(batch: PlayHistoryBatch => Unit)
  
  def write(roomId: java.util.UUID, game: model.Game, stake: model.Stake, play: model.Play) {
    batch(play.id) { batch =>
      batch.writeEntry(
        roomId,
        play.started, play.ended,
        game.`type`, game.limit,
        stake,
        play.button,
        play.board,
        play.pot.total, play.rake.map(_.total))
      
      play.seating.map { case (player, pos) =>
        val amount = play.stacks(player)
        val net = play.net(player)
        val cards: poker.Cards = play.knownCards.getOrElse(player, null)
        batch.writePosition(pos, player, amount, net, cards)
      }

      play.actions.foreach { case (street, actions) =>
        actions.foreach { case model.Action(player, bet, at) =>
          batch.writeAction(at, player, street.toString(), bet)
        }
      }
    }
  }
}