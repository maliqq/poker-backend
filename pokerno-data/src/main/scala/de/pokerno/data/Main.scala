package de.pokerno.data

import pokerdb.PokerDB
import java.util.UUID

object Main {

  def main(args: Array[String]) {
    val props = new java.util.Properties
    props.load(new java.io.FileInputStream("./etc/database.properties"))
    
    val session = db.Connection.connector(
        "org.postgresql.Driver",
        props.getProperty("database.url"),
        props.getProperty("database.username"),
        props.getProperty("database.password")
        )()
    
    session.setLogger(println(_))
    session.bindToCurrentThread
    
    println(pokerdb.model.PlaySession.create(UUID.randomUUID(), UUID.randomUUID(), 1, 10000).id)
    
    val (room, game, mix, stake) = PokerDB.roomsWithGamesAndStakes.head
    println(room)
    
    session.close
  }
  
}
