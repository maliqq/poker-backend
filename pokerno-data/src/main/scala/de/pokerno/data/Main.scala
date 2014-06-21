package de.pokerno.data

import de.pokerno.data.pokerdb.PokerDB

object Main {

  def main(args: Array[String]) {
    val props = new java.util.Properties
    props.load(new java.io.FileInputStream("./etc/database.properties"))
    
    val session = PokerDB.Connection.connect(
        "org.postgresql.Driver",
        props.getProperty("database.url"),
        props.getProperty("database.username"),
        props.getProperty("database.password")
        )
    
    session.setLogger(println(_))
    session.bindToCurrentThread
    
    val seat = new PokerDB.Seat(java.util.UUID.randomUUID(), 1, java.util.UUID.randomUUID(), 10000, "none")
    
    println(PokerDB.createSeat(seat).id)
    
    println(PokerDB.createSeat(seat).id)
    
    val (room, game, mix, stake) = PokerDB.roomsWithGamesAndStakes.head
    println(room)
    
    session.close
  }
  
}