package de.pokerno.data

import de.pokerno.data.snapshot.PostgreSql

object Main {

  def main(args: Array[String]) {
    val props = new java.util.Properties
    props.load(new java.io.FileInputStream("./etc/database.properties"))
    
    val session = PostgreSql.Connection.connect(
        "org.postgresql.Driver",
        props.getProperty("database.url"),
        props.getProperty("database.username"),
        props.getProperty("database.password")
        )
    
    session.setLogger(println(_))
    session.bindToCurrentThread
    
    val seat = new PostgreSql.Seat(java.util.UUID.randomUUID(), 1, java.util.UUID.randomUUID(), 10000, "none")
    
    println(PostgreSql.createSeat(seat).id)
    
    println(PostgreSql.createSeat(seat).id)
    
    val (room, game, mix, stake) = PostgreSql.roomsWithGamesAndStakes.head
    println(room)
    
    session.close
  }
  
}
