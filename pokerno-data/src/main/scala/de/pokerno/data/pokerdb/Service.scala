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
    val rooms = PokerDB.Node.getRooms(nodeId)
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
    PokerDB.Node.updateMetrics(nodeId, metrics)
  }
  
  def getRoom(id: String): Future[thrift.Room] = Future {
    val (room, game, mix, stake) = PokerDB.Room.get(id)
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
    
    PokerDB.Room.updateState(id, newState)
  }
  
  def reportRoomMetrics(roomId: String, metrics: thrift.metrics.Room): Future[Unit] = Future {
    PokerDB.Room.updateMetrics(roomId, metrics)
  }
  
  def startSession(roomId: String, player: String, pos: Int, amount: Double): Future[Unit] = Future {
    PokerDB.PlaySession.create(roomId, player, pos, amount)
  }
  
  def endSession(roomId: String, player: String, pos: Int, amount: Double): Future[Unit] = Future {
    PokerDB.PlaySession.end(roomId, player)
  }
  
}
