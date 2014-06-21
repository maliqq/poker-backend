package de.pokerno.data

object Database {

  trait Client {
    
    def updateRoomState(state: String)(implicit roomId: String)
    def createSeat(pos: Int, player: String)(implicit roomId: String)
    def updateSeatState(pos: Int, player: String, state: String)(implicit roomId: String)
    def destroySeat(pos: Int, player: String)(implicit roomId: String)
    
  }
  
}
