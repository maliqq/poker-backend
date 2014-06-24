package de.pokerno.data.pokerdb

import com.twitter.util.Future
import org.slf4j.LoggerFactory
import java.util.UUID

object Service {
  import org.apache.thrift.protocol.TBinaryProtocol
  import org.apache.thrift.protocol.TProtocolFactory
  import com.twitter.finagle.builder.ServerBuilder
  import com.twitter.finagle.thrift.ThriftServerFramedCodec
  import com.twitter.finagle.{Service => FinagleService}
  import org.apache.thrift.protocol.TProtocolFactory

  def start(addr: java.net.InetSocketAddress) {
    val protocol = new TBinaryProtocol.Factory()
    val processor = new Service()
    val service = new thrift.PokerDB.FinagledService(processor, protocol)
    val server = ServerBuilder().
      codec(ThriftServerFramedCodec()).
      bindTo(addr).
      name("pokerdb").
      build(service)
  }
}

class Service extends thrift.PokerDB.FutureIface {
  import ThriftConversions._
  import de.pokerno.protocol.{thrift => protocol}

  implicit def uuidFromString(s: String): UUID = UUID.fromString(s)
  
  val log = LoggerFactory.getLogger(getClass)

  def getRooms(nodeId: String): Future[Seq[thrift.Room]] = Future {
    val rooms = PokerDB.getRooms(nodeId)
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
  
  def reportNodeMetrics(nodeId: String, metrics: thrift.metrics.Node): Future[Unit] = Future {
    PokerDB.updateNodeMetrics(nodeId, metrics)
  }
  
  def getRoom(id: String): Future[thrift.Room] = Future {
    val (room, game, mix, stake) = PokerDB.getRoom(id)
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
    
    PokerDB.updateRoomState(id, newState)
  }
  
  def reportRoomMetrics(roomId: String, metrics: thrift.metrics.Room): Future[Unit] = Future {
    PokerDB.updateRoomMetrics(roomId, metrics)
  }
  
  def registerSeat(roomId: String, pos: Int, player: String, amount: Double): Future[Unit] = Future {
    val seat = new PokerDB.Seat(roomId, pos, player, amount, "taken")
    
    log.info("registering seat: {}", seat)
    PokerDB.createSeat(seat)
  }
  
  def changeSeatState(roomId: String, pos: Int, player: String, state: protocol.SeatState): Future[Unit] = Future {
    PokerDB.updateSeatState(roomId, pos, player, state.name.toLowerCase)
  }
  
  def unregisterSeat(roomId: String, pos: Int, player: String, amount: Double): Future[Unit] = Future {
    PokerDB.deleteSeat(roomId, player)
  }
  
}
