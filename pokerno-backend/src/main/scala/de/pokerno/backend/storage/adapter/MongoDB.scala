package de.pokerno.backend.storage.adapter

import org.apache.commons.codec.binary.Base64
import de.pokerno.backend.storage.{ PlayHistory, Store }
import de.pokerno.poker.Cards
import com.mongodb._

object MongoDB {

  final val defaultDbName = "poker"
  final val defaultCollectionName = "plays"

  class Client(
      val host: String = "localhost",
      val port: Int = 27017,
      val dbName: String = defaultDbName,
      val collectionName: String = defaultCollectionName) extends Store.Client {

    private val client = new MongoClient(host, port)
    private val db = client.getDB(dbName)

    val collection = db.getCollection(collectionName)

    def write(entry: PlayHistory.Entry) {
      val b = BasicDBObjectBuilder.start()

      // uuid
      b.add("external_id", entry.id)

      // timestamps
      b.add("start_at", entry.startAt)
      b.add("stop_at", entry.stopAt)

      // pot
      b.add("pot", entry.pot.toDouble)
      entry.rake.map { rake ⇒
        b.add("rake", rake.toDouble)
      }

      // winners
      val winners = BasicDBObjectBuilder.start()
      entry.winners foreach {
        case (winner, amount) ⇒
          winners.add(winner, amount.toDouble)
      }
      b.add("winners", winners)

      // known cards
      val knownCards = BasicDBObjectBuilder.start()
      entry.knownCards foreach {
        case (player, cards) ⇒
          val binaryCards: Array[Byte] = cards.map(_.toByte).toArray
          knownCards.add(player, binaryCards)
      }
      b.add("known_cards", knownCards)

      // TODO actions

      collection.insert(b.get())
    }
  }
}
