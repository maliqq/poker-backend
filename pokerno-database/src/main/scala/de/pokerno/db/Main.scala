package de.pokerno.db

object Main {

  def main(args: Array[String]) {
    val session = Connection.connect()
    session.bindToCurrentThread
    
    val (room, game, stake) = Database.roomsWithGamesAndStakes.head
    println(room)
  }
  
}
