package de.pokerno.backend.storage

import com.mongodb.casbah.Imports._

import math.{BigDecimal => Decimal}
import de.pokerno.backend.{BatchedStorage, PlayHistoryBatch}
import de.pokerno.{model, poker}

object MongoDB {
  
  object Connection {
    def connect(host: String, port: Int) = {
      MongoClient(host, port)
    }
  }
  
  class Batch(_id: java.util.UUID, _collection: MongoCollection) extends PlayHistoryBatch {
    var _playObj: MongoDBObject = null
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
      rake:     Option[Decimal],        // rake
      uncalled: Decimal
    ) {
      _playObj = MongoDBObject(
          "room_id" -> roomId,
          "started" -> started,
          "ended" -> ended,
          "game" -> game.toString(),
          "limit" -> limit.toString(),
          "stake" -> MongoDBObject(
              "big_blind" -> stake.bigBlind,
              "small_blind" -> stake.smallBlind,
              "ante" -> stake.ante,
              "bring_in" -> stake.bringIn
            ),
          "button" -> button,
          "board" -> (board: Array[Byte]),
          "pot" -> pot
        )
    }
    
    var _positions = collection.mutable.Map[String, MongoDBObject]()
    def writePosition(
      pos: Int,           // position at the table
      player: String,     // player id/uuid
      amount: Decimal,    // amount at start of the deal
      net: Decimal,       // amount won/lost
      cards: poker.Cards        // cards (if shown at showdown)
    ) {
      _positions(player) = MongoDBObject(
          "pos" -> pos,
          "amount" -> amount,
          "net" -> net,
          "cards" -> (cards: Array[Byte]) 
      )
    }

    import collection.mutable.ListBuffer
    var _actions = collection.mutable.Map[String, ListBuffer[MongoDBObject]]()
    def writeAction(
      at: java.util.Date,     // event date
      player: String,         // player acted
      street: String,         // street
      bet: model.Bet,          // card or chip actin
      isAllIn: Option[Boolean],
      isTimeout: Option[Boolean]
    ) {
      if (!_actions.contains(street)) {
        _actions(street) = ListBuffer.empty
      }
      val betObj = MongoDBObject(
          "street" -> street,
          "bet" -> bet.name,
          "check" -> bet.isCheck,
          "fold" -> bet.isFold
      )
      if (bet.isCall) betObj += ("call" -> (bet.toActive.amount))
      if (bet.isRaise) betObj += ("raise" -> (bet.toActive.amount))
      _actions(street) += betObj 
    }

    def write() = {
      _playObj += (
          "actions" -> _actions,
          "positions" -> _positions
        )
      _collection.insert(_playObj)
    }
  }
  
  class Storage(client: MongoClient) extends BatchedStorage {
    val db      = client("plays")
    val deals   = db("deals")
    
    def batch(id: java.util.UUID)(f: PlayHistoryBatch => Unit) = f(new Batch(id, deals))
  }
}
