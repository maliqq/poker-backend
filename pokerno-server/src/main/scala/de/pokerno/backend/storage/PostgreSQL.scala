package de.pokerno.backend.storage

import math.{BigDecimal => Decimal}
import de.pokerno.backend.{BatchedStorage, PlayHistoryBatch}
import de.pokerno.{model, poker}
import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.internals.FieldMetaData

object PostgreSQL {
  
  class Play(
      var id: java.util.UUID,
      @Column("room_id") var roomId: java.util.UUID,
      var started: java.sql.Timestamp,
      var ended: java.sql.Timestamp,
      @Column("game_type") var game: String,
      @Column("game_limit") var limit: String,
      @Column("big_blind") var bigBlind: Double,
      @Column("small_blind") var smallBlind: Double,
      @Column(name = "ante", optionType=classOf[Double]) var ante: Option[Double],
      @Column(name = "bring_in", optionType=classOf[Double]) var bringIn: Option[Double],
      var button: Int,
      var board: Array[Byte],
      var pot: Double,
      var rake: Double
  ) extends KeyedEntity[java.util.UUID] {
    def this() = this(null, null, null, null, "", "", 0, 0, None, None, 0, null, 0, 0)
  }
  
  class Position(
      @Column("play_id") var playId: java.util.UUID,
      @Column("player_id") var playerId: java.util.UUID,
      var pos: Int,
      var amount: Double,
      var net: Double,
      var netBB: Double,
      var cards: Array[Byte]
  ) {
    def this() = this(null, null, 0, 0, 0, 0, null)
  }
  
  class Action(
      @Column("play_id") var playId: java.util.UUID,
      @Column("player_id") var playerId: java.util.UUID,
      var at: java.sql.Timestamp,
      var street: String,
      var bet: String,
      var check: Boolean,
      var fold: Boolean,
      @Column(name="call", optionType = classOf[Double]) var call: Option[Double],
      @Column(name="raise", optionType = classOf[Double]) var raise: Option[Double],
      var cards: Array[Byte],
      var muck: java.lang.Boolean
  ) {
    def this() = this(null, null, null, "", "", false, false, None, None, null, null)
  }
  
  object PlayHistoryDB extends Schema {
    val plays = table[Play]("poker_plays")
    val positions = table[Position]("poker_play_positions")
    val actions = table[Action]("poker_play_actions")
  }
  
  implicit def date2timestamp(d: java.util.Date): java.sql.Timestamp = java.sql.Timestamp.from(d.toInstant)
  
  class Batch(_id: java.util.UUID) extends PlayHistoryBatch {
    var _play: Play = null
    
    def writeEntry(
      roomId:   java.util.UUID,
      started:  java.util.Date,
      ended:    java.util.Date,
      game:     model.GameType,
      limit:    model.GameLimit,
      stake:    model.Stake,
      button:   Int,            // staring position at the table
      board:    poker.Cards,
      pot:      Decimal,           // total size of the pot
      rake:     Decimal        // rake
    ) {
      _play = new Play(
          _id,
          roomId,
          started, ended,
          game.toString(), limit.toString(),
          stake.bigBlind.toDouble, stake.smallBlind.toDouble, stake.ante.map(_.toDouble), stake.bringIn.map(_.toDouble),
          button,
          (board: Array[Byte]),
          pot.toDouble, rake.toDouble
      )
    }
    
    val _positions = collection.mutable.ListBuffer[Position]()
    def writePosition(
      pos: Int,           // position at the table
      player: String,     // player id/uuid
      amount: Decimal,    // amount at start of the deal
      net: Decimal,       // amount won/lost
      cards: poker.Cards        // cards (if shown at showdown)
    ) {
      _positions += new Position(
          _id,
          java.util.UUID.fromString(player),
          pos,
          amount.toDouble, net.toDouble, net.toDouble / _play.bigBlind,
          (cards: Array[Byte])
      )
    }

    val _actions = collection.mutable.ListBuffer[Action]()
    def writeAction(
      at: java.util.Date,     // event date
      player: String,         // player acted
      street: String,         // street
      bet: model.Bet          // card or chip actin
    ) {
      _actions += new Action(
          _id,
          java.util.UUID.fromString(player),
          at,
          street,
          bet.name,
          bet.isCheck,
          bet.isFold,
          if (bet.isRaise) Some(bet.toActive.amount.toDouble) else None,
          if (bet.isCall) Some(bet.toActive.amount.toDouble) else None,
          null, null
      )
    }
    
    def write() {
      PlayHistoryDB.plays.insert(_play)
      PlayHistoryDB.positions.insert(_positions)
      PlayHistoryDB.actions.insert(_actions)
    }
  }
  
  class Storage extends BatchedStorage {
    def batch(id: java.util.UUID)(f: PlayHistoryBatch => Unit) = {
      val b = new Batch(id)
      f(b)
      b.write()
    }
  }
}
