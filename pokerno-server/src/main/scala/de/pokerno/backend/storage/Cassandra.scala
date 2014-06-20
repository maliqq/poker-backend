package de.pokerno.backend.storage

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.BatchStatement

import de.pokerno.model.{Play, Action, BetType}

object Cassandra {
  
  def connect(node: String, keyspace: String) = {
    val cluster = Cluster.builder().
                          addContactPoint(node).
                          withoutJMXReporting().
                          withoutMetrics().
                          build()
                          
    val metadata = cluster.getMetadata()
    cluster.connect(keyspace)
  }
  
  class Client(node: String, keyspace: String) {
    private def session = connect(node, keyspace)
    val insertDeal    = session.prepare("INSERT INTO deals (room_id, deal_id, button, started, ended, seating, stacks, net, known_cards) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")
    val insertAction  = session.prepare("INSERT INTO deal_actions (deal_id, time, player_id, street, bet_marker, fold, check, call, raise, cards, muck) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
    import collection.JavaConverters._
    
    def write(id: java.util.UUID, play: Play) {
      val batch = new BatchStatement
      val bound = insertDeal.bind()
      bound.bind(
          id,
          play.id,
          play.button:          java.lang.Integer,
          
          play.started,
          play.ended,
          
          play.seating,
          play.stacks,
          play.net,
          play.knownCards
      )
      batch.add(bound)
      
      play.actions.foreach { case (street, actions) =>
        actions.foreach { case Action(player, bet, at) =>
          val betMarker: String = if (bet.isForced) bet.name else null
          val raise: java.lang.Double = if (bet.isRaise) bet.toActive.amount.toDouble else null
          val call: java.lang.Double = if (bet.isCall) bet.toActive.amount.toDouble else null
          
          val bound = insertAction.bind(id, at, player, street, betMarker,
              bet.isFold:   java.lang.Boolean,
              bet.isCheck:  java.lang.Boolean,
              bet.isCall:   java.lang.Boolean,
              raise, call, null, null)
          batch.add(bound)
        }
      }
      
      session.execute(batch)
    }
  }
  
  object Schemas {
    final val creation = "CREATE KEYSPACE poker WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
    final val deals_v1 = """
CREATE TABLE deals (
  room_id      uuid,
  deal_id      uuid,
  
  game_type    ascii,
  game_limit   ascii,

  bb           decimal,
  sb           decimal,
  ante         decimal,

  button       int,
  
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
CREATE TABLE deal_actions (
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
