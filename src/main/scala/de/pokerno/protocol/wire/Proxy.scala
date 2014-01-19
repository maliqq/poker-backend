package de.pokerno.protocol.wire

import de.pokerno.model
import de.pokerno.poker
import de.pokerno.protocol.HasPlayer
import reflect._
import math.{BigDecimal => Decimal}

import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
case class Range(
  @BeanProperty
  var min: java.lang.Double,
  @BeanProperty
  var max: java.lang.Double
) {
  def this() = this(null, null)
}

@MsgPack
case class Box(
  @BeanProperty
  var pos: Integer,
  
  var player: model.Player
) extends HasPlayer {
  def this() = this(null, null)
}

@MsgPack
case class Bet(
  @BeanProperty
  var `type`: BetSchema.BetType,
  @BeanProperty
  var amount: java.lang.Double
) {
  def this() = this(null, null)
}

@MsgPack
class Game {
  @BeanProperty
  var `type`: GameSchema.GameType = null
  @BeanProperty
  var limit: GameSchema.GameLimit = null
  @BeanProperty
  var tableSize: Integer = null
}

@MsgPack
class Mix {
  @BeanProperty
  var `type`: MixSchema.MixType = null
  @BeanProperty
  var tableSize: Integer = null
}

@MsgPack
class Variation {
  @BeanProperty
  var `type`: VariationSchema.VariationType = null
  @BeanProperty
  var mix: Mix = null
  @BeanProperty
  var game: Game = null
}

@MsgPack
case class Hand(
  @BeanProperty
  var cards: ByteString,
  @BeanProperty
  var rank: HandSchema.RankType,
  @BeanProperty
  var value: ByteString,
  @BeanProperty
  var high: ByteString,
  @BeanProperty
  var kicker: ByteString,
  @BeanProperty
  var string: String = ""
) {
  def this() = this(null, null, null, null, null, "")
}

@MsgPack
class Seat {
  @BeanProperty
  var state: SeatSchema.SeatState = null
  @BeanProperty
  var player: String = null
  @BeanProperty
  var stackAmount: java.lang.Double = null
  @BeanProperty
  var putAmount: java.lang.Double = null
}

@MsgPack
class Stake {
  @BeanProperty
  var bigBlind: java.lang.Double = null
  @BeanProperty
  var smallBlind: java.lang.Double = null
  @BeanProperty
  var ante: java.lang.Double = null
  @BeanProperty
  var bringIn: java.lang.Double = null
}

@MsgPack
case class Table(
  @BeanProperty
  var size: Integer,
  @BeanProperty
  var button: Integer = null,
  @BeanProperty
  var seats: java.util.ArrayList[Seat] = null,
  @BeanProperty
  var state: TableSchema.TableState = null
) {
  def this() = this(null)
}