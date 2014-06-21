package de.pokerno.data.snapshot

import com.mongodb.casbah.Imports._
import de.pokerno.data.Database

object MongoDB {
  
  object Connection {
    def connect(host: String, port: Int) = {
      MongoClient(host, port)
    }
  }
  
  abstract class Client(host: String, port: Int, dbName: String) extends Database.Client {
    
    val client  = Connection.connect(host, port)
    val db      = client(dbName)
    val rooms   = db("rooms")
    
    def updateSeatState(pos: Int, player: String, state: String)(implicit roomId: String) {
      rooms.update(
          MongoDBObject(),
          MongoDBObject(),
          upsert = true)
    }
    
    def destroySeat(pos: Int, player: String)(implicit roomId: String) {
      rooms.remove(MongoDBObject(
          
          ))
    }

  }
}
