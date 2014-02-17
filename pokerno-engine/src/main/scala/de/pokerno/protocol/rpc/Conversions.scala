package de.pokerno.protocol.rpc

import de.pokerno.model.{ Table, Game, Stake, Variation }
import de.pokerno.protocol.wire
import de.pokerno.protocol.wire.Conversions._
import de.pokerno.protocol.Conversions._
import proto.rpc._

object Conversions {

  import collection.JavaConversions._

  implicit def createTable(_table: wire.Table): Table = {
    val table = new Table(_table.size)
    var pos = 0
    for (seat ← _table.seats) {
      if (seat != null && seat.player != null && seat.stackAmount != null)
        table.addPlayer(pos, seat.player, Some(seat.stackAmount))
      pos += 1
    }
    table.button.current = _table.button
    table
  }

  import proto.wire.VariationSchema
  implicit def createVariation(_variation: wire.Variation): Variation =
    _variation.`type` match {
      case VariationSchema.VariationType.GAME ⇒
        new Game(
          _variation.game.`type`,
          Some(_variation.game.limit),
          _variation.game.tableSize match {
            case null ⇒ None
            case n    ⇒ Some(n)
          }
        )
      case VariationSchema.VariationType.MIX ⇒
        // TODO
        null
    }

  implicit def createStake(_stake: wire.Stake): Stake =
    new Stake(_stake.bigBlind,
      _stake.smallBlind match {
        case null ⇒ None
        case n    ⇒ Some(n)
      },
      _stake.ante match {
        case null ⇒ Right(false)
        case n    ⇒ Left(n)
      }
    )

}
