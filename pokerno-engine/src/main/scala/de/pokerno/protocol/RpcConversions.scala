package de.pokerno.protocol

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.{ model, poker, gameplay }

import com.dyuproject.protostuff.ByteString

object RpcConversions {

  import CommonConversions._
  import WireConversions._
  import proto.rpc._
  import de.pokerno.protocol.rpc._
  import collection.JavaConversions._
  import proto.wire.VariationSchema

  implicit def createTable(_table: wire.Table): model.Table = {
    val table = new model.Table(_table.size)
    var pos = 0
    for (seat ← _table.seats) {
      if (seat != null && seat.player != null && seat.stackAmount != null)
        table.takeSeat(pos, seat.player, Some(seat.stackAmount))
      pos += 1
    }
    table.button.current = _table.button
    table
  }

  implicit def createVariation(_variation: wire.Variation): model.Variation =
    _variation.`type` match {
      case VariationSchema.VariationType.GAME ⇒
        new model.Game(
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

  implicit def createStake(_stake: wire.Stake): model.Stake =
    new model.Stake(_stake.bigBlind,
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
