package de.pokerno.db

import com.twitter.util.Future
import org.slf4j.LoggerFactory

class Service extends de.pokerno.db.thrift.Database.FutureIface {
  import ThriftConversions._
  import de.pokerno.protocol.{thrift => protocol}

  implicit def uuidFromString(s: String): java.util.UUID = java.util.UUID.fromString(s)
  
  val log = LoggerFactory.getLogger(getClass)

  def restoreRooms(nodeId: String): Future[Seq[thrift.Room]] = Future {
    val rooms = Database.getRooms(nodeId)
    rooms.map { case (room, game, mix, stake) =>
      thrift.Room(
        room.id.toString(),
        room.state,
        room.name,
        game.map { g => g: protocol.Game },
        mix.map { m => m: protocol.Mix },
        stake
      )
    }.toSeq
  }
  
  def getRoom(id: String): Future[thrift.Room] = Future {
    val (room, game, mix, stake) = Database.getRoom(id)
    thrift.Room(
        room.id.toString(),
        room.state,
        room.name,
        game.map { g => g: protocol.Game },
        mix.map { m => m: protocol.Mix },
        stake
      )
  }
  
  def createRoom(nodeId: String, name: String, game: protocol.Game, stake: protocol.Stake): Future[thrift.Room] = Future {
    throw new UnsupportedOperationException("createRoom not implemented")
  }
  
  def changeRoomState(id: String, state: thrift.State): Future[Unit] = Future {
    val newState = state: String
    
    log.info("room %s changed state to %s" format(id, newState))
    
    Database.updateRoomState(id, newState)
  }
  
  def registerSeat(roomId: String, pos: Int, player: String, amount: Double): Future[Unit] = Future {
    val seat = new Database.Seat(roomId, pos, player, amount, "taken")
    
    log.info("registering seat: {}", seat)
    Database.createSeat(seat)
  }
  
  def changeSeatState(roomId: String, pos: Int, player: String, state: protocol.SeatState): Future[Unit] = Future {
    Database.updateSeatState(roomId, pos, player, state.name.toLowerCase)
  }
  
  def unregisterSeat(roomId: String, pos: Int, player: String, amount: Double): Future[Unit] = Future {
    Database.deleteSeat(roomId, pos, player)
  }
  
}
