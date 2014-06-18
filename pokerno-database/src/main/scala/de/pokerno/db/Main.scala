package de.pokerno.db

object Main {

  def main(args: Array[String]) {
    val props = new java.util.Properties
    props.load(new java.io.FileInputStream("./etc/database.properties"))
    
    val session = Connection.connect(
        "org.postgresql.Driver",
        props.getProperty("database.url"),
        props.getProperty("database.username"),
        props.getProperty("database.password")
        )
    
    session.setLogger(println(_))
    session.bindToCurrentThread
    
    val seat = new Database.Seat(java.util.UUID.randomUUID(), 1, java.util.UUID.randomUUID(), 10000, "none")
    
    println(Database.createSeat(seat).id)
    
    println(Database.createSeat(seat).id)
    
    val (room, game, mix, stake) = Database.roomsWithGamesAndStakes.head
    println(room)
    
    session.close
  }
  
}
