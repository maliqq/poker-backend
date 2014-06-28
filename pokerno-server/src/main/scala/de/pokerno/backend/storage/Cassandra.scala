package de.pokerno.backend.storage

import com.datastax.driver.core.{Cluster, Session, BatchStatement}

import math.{BigDecimal => Decimal}
import de.pokerno.backend.{BatchedStorage, PlayHistoryBatch}
import de.pokerno.{model, poker}

object Cassandra {
  
  object Connection {
    def connect(node: String, keyspace: String) = {
      val cluster = Cluster.builder().
                            addContactPoint(node).
                            withoutJMXReporting().
                            withoutMetrics().
                            build()
                            
      val metadata = cluster.getMetadata()
      cluster.connect(keyspace)
    }
  }

  class Batch(_id: java.util.UUID, session: Session) extends PlayHistoryBatch {
    val insertPlay    = session.prepare("INSERT INTO plays (room_id, deal_id, button, started, ended, seating, stacks, net, known_cards) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")
    val insertAction  = session.prepare("INSERT INTO actions (deal_id, time, player_id, street, bet_marker, fold, check, call, raise, cards, muck) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")

    private val _batch = new BatchStatement

    val _seating = new java.util.HashMap[String, java.lang.Integer]()
    val _stacks = new java.util.HashMap[String, java.lang.Double]()
    val _net = new java.util.HashMap[String, java.lang.Double]()
    val _cards = new java.util.HashMap[String, java.nio.ByteBuffer]()

    def writeEntry(
      roomId: java.util.UUID,
      started: java.util.Date,
      ended: java.util.Date,
      game: model.Game,
      stake: model.Stake,
      button: Int,            // staring position at the table
      board: poker.Cards,
      pot: Decimal,           // total size of the pot
      rake: Option[Decimal],        // rake
      uncalled: Decimal
    ) {
      val bound = insertPlay.bind(
          roomId, _id,
          
          button: java.lang.Integer,
          
          started, ended,
          
          _seating, _stacks, _net, _cards
      )
      _batch.add(bound)
    }
    
    def writePosition(
      pos: Int,           // position at the table
      player: String,     // player id/uuid
      amount: Decimal,    // amount at start of the deal
      net: Decimal,       // amount won/lost
      cards: poker.Cards        // cards (if shown at showdown)
    ) {
      _seating.put(player, pos)
      _stacks.put(player, amount.toDouble)
      _net.put(player, net.toDouble)
      _cards.put(player, java.nio.ByteBuffer.wrap(cards:Array[Byte]))
    }

    def writeAction(
      at: java.util.Date,     // event date
      player: String,         // player acted
      street: String,         // street
      bet: model.Bet,          // card or chip actin
      isAllIn: Option[Boolean],
      isTimeout: Option[Boolean]
    ) {
      val betMarker: String = if (bet.isForced) bet.name else null
      val raise: java.lang.Double = if (bet.isRaise) bet.toActive.amount.toDouble else null
      val call: java.lang.Double = if (bet.isCall) bet.toActive.amount.toDouble else null
      
      val bound = insertAction.bind(
          _id,                  // deal_id
          at,                   // time
          player,               // player_id
          street.toString(),    // street
          betMarker,            // bet_marker
          bet.isFold:   java.lang.Boolean,  // fold
          bet.isCheck:  java.lang.Boolean,  // check
          call,                 // call
          raise,                // raise
          null,                 // cards
          null                  // muck
        )
      _batch.add(bound)
    }

    def write() = session.execute(_batch)
  }
  
  class Storage(session: Session) extends BatchedStorage {
    //private def session = Connection.connect(node, keyspace)
    import collection.JavaConverters._
    def batch(id: java.util.UUID)(f: PlayHistoryBatch => Unit) = {
      f(new Batch(id, session))
    }
  }
  
  object Schemas {
    final val creation = "CREATE KEYSPACE poker WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
    final val deals_v1 = """
CREATE TABLE plays (
  room_id      uuid,
  deal_id      uuid,
  
  game_type    ascii,
  game_limit   ascii,

  bb           decimal,
  sb           decimal,
  ante         decimal,

  button       int,
      
  board        blob, 
  
  started      timestamp,
  ended        timestamp,
  
  seating      map <varchar, int>,
  stacks       map <varchar, double>,
  net          map <varchar, double>,
  known_cards  map <varchar, blob>,
  
  PRIMARY KEY(room_id, deal_id)
);
  """
    final val deal_actions_v1 = """
CREATE TABLE actions (
  deal_id     uuid,
  time        timestamp,
  player_id   varchar,
  
  street      ascii,
  bet_marker  ascii,
  
  fold        boolean,
  check       boolean,
  call        double,
  raise       double,
  
  cards       blob,
  muck        boolean,
  
  PRIMARY KEY(deal_id, time, player_id)
);
    """
  }
}
