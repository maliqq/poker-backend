package de.pokerno.protocol.wire

import reflect._

import com.dyuproject.protostuff.ByteString
import org.msgpack.annotation.{ Message ⇒ MsgPack }

@MsgPack
case class Range(
    @BeanProperty var min: java.lang.Double,
    @BeanProperty var max: java.lang.Double) {
  def this() = this(null, null)
}

@MsgPack
case class Box(
    @BeanProperty var pos: Integer,

    @BeanProperty var player: String) {
  def this() = this(null, null)
}

@MsgPack
case class Bet(
    @BeanProperty var `type`: BetSchema.BetType,
    @BeanProperty var amount: java.lang.Double = null) {
  def this() = this(null)
}

@MsgPack
class Game(
    @BeanProperty var `type`: GameSchema.GameType,

    @BeanProperty var limit: GameSchema.GameLimit,

    @BeanProperty var tableSize: Integer = null) {
  def this() = this(null, null)
}

@MsgPack
case class Mix(
    @BeanProperty var `type`: MixSchema.MixType,

    @BeanProperty var tableSize: Integer = null) {
  def this() = this(null)
}

@MsgPack
case class Variation(
    @BeanProperty var `type`: VariationSchema.VariationType,

    @BeanProperty var mix: Mix = null,

    @BeanProperty var game: Game = null) {
  def this() = this(null)
}

@MsgPack
case class Hand(
    @BeanProperty var cards: ByteString,
    @BeanProperty var rank: HandSchema.RankType,
    @BeanProperty var value: ByteString,
    @BeanProperty var high: ByteString,
    @BeanProperty var kicker: ByteString,
    @BeanProperty var string: String = "") {
  def this() = this(null, null, null, null, null, "")
}

@MsgPack
class Seat(
    @BeanProperty var state: SeatSchema.SeatState,

    @BeanProperty var player: String,

    @BeanProperty var stackAmount: java.lang.Double,

    @BeanProperty var putAmount: java.lang.Double = null) {
  def this() = this(null, null, null)
}

@MsgPack
case class Stake(
    @BeanProperty var bigBlind: java.lang.Double,

    @BeanProperty var smallBlind: java.lang.Double = null,

    @BeanProperty var ante: java.lang.Double = null,

    @BeanProperty var bringIn: java.lang.Double = null) {
  def this() = this(null)
}

@MsgPack
case class Table(
    @BeanProperty var size: Integer,

    @BeanProperty var button: Integer = null,

    @BeanProperty var seats: java.util.ArrayList[Seat] = null,

    @BeanProperty var state: TableSchema.TableState = null) {
  def this() = this(null)
}
