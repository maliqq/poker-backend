package de.pokerno.data.pokerdb

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.internals.FieldMetaData
import java.util.UUID

object PokerDB extends Schema {
  sealed case class Node(
      @Column("total_connections_count") var totalConnectionsCount: Long,
      @Column("player_connections_count") var playerConnectionsCount: Long,
      @Column("offline_players_count") var offlinePlayersCount: Long,
      @Column("connects_rate") var connectsRate: Double,
      @Column("disconnects_rate") var disconnectsRate: Double,
      @Column("messages_received_rate") var messagesReceivedRate: Double
  ) extends KeyedEntity[UUID] {
    var id: UUID = null
  }
  
  sealed case class Room(
      @Column(name = "node_id") var nodeId: UUID,
      var state: String,
      var name: String,
      @Column(name = "game_id", optionType = classOf[Long]) var gameId: Option[Long],
      @Column(name = "mix_id", optionType = classOf[Long]) var mixId: Option[Long],
      @Column("stake_id") var stakeId: Long,
      @Column("players_count") var playersCount: Long,
      @Column("waiting_count") var waitingCount: Long,
      @Column("watching_count") var watchingCount: Long,
      @Column("plays_rate") var playsRate: Double,
      @Column("players_per_flop") var playersPerFlop: Double,
      @Column("average_pot") var average_pot: Double
      ) extends KeyedEntity[UUID] {
    var id: UUID = null
    def this() = this(null, "", "", None, None, 0, 0, 0, 0, 0, 0, 0)
  }
  
  sealed case class Stake(
      @Column("big_blind") var bigBlind: Double,
      @Column("small_blind") var smallBlind: Double,
      @Column(optionType = classOf[Double])  var ante: Option[Double],
      @Column("buy_in_min") var buyInMin: Int,
      @Column("buy_in_max") var buyInMax: Int,
      @Column(name = "currency_id", optionType=classOf[Long]) var currencyId: Option[Long]) extends KeyedEntity[Long] {
    var id: Long = 0
    def this() = this(0, 0, None, 0, 0, None)
  }
  
  sealed case class Game(
      var variation: String,
      var limit: Option[String],
      @Column(optionType = classOf[Int]) var speed: Option[Int],
      @Column("table_size") var tableSize: Int) extends KeyedEntity[Long] {
    var id: Long = 0
    def this() = this("", None, None, 0)
  }
  
  sealed case class Seat(
      @Column("room_id") var roomId: UUID,
      var pos: Int,
      @Column("player_id") var playerId: UUID,
      var stack: Double,
      var state: String
  ) extends KeyedEntity[Long] {
    var id: Long = 0
    def this() = this(null, 0, null, 0, "")
  }
  
  sealed case class Mix(
      var variation: String,
      @Column(optionType = classOf[Int]) var speed: Option[Int],
      @Column("table_size") var tableSize: Int) extends KeyedEntity[Long] {
    var id: Long = 0
    def this() = this("", None, 0)
  }
  
  sealed case class Tournament(
      @Column(name = "game_id", optionType = classOf[Int]) var gameId: Option[Long],
      @Column(name = "mix_id", optionType = classOf[Int]) var mixId: Option[Long],
      @Column("buy_in_id") var buyInId: Long
  ) extends KeyedEntity[UUID]{
    var id: UUID = null
    def this() = this(None, None, 0)
  }
  
  sealed case class TournamentBuyIn(
      @Column(name = "currency_id", optionType=classOf[Long]) var currencyId: Option[Long], 
      var price: Double,
      var fee: Double
    ) {
    var id: Long = 0
    def this() = this(None, 0, 0) 
  }
  
  val nodes     = table[Node]("nodes")
  val rooms     = table[Room]("poker_rooms")
  val games     = table[Game]("poker_variations")
  val mixes     = table[Mix]("poker_variations")
  val stakes    = table[Stake]("poker_stakes")
  val seats     = table[Seat]("poker_seats")
  
  val tournaments       = table[Tournament]("poker_tournaments")
  val tournamentBuyIns  = table[TournamentBuyIn]("poker_tournament_buy_ins")
  
  lazy val roomsWithGamesAndStakes = join(rooms, games.leftOuter, mixes.leftOuter, stakes)((room, game, mix, stake) =>
    select((room, game, mix, stake))
    on(room.gameId === game.map(_.id), room.mixId === mix.map(_.id), room.stakeId === stake.id)
  )
  
  def getRoomStake(roomId: UUID): Stake = {
    join(rooms, stakes)((room, stake) =>
      where(room.id === roomId)
      select(stake)
      on(room.stakeId === stake.id)
    ).head
  }
  
  def getTournamentBuyIn(tournamentId: UUID): TournamentBuyIn = {
    join(tournaments, tournamentBuyIns)((tournament, tournamentBuyIn) =>
      where(tournament.id === tournamentId)
      select(tournamentBuyIn)
      on(tournament.buyInId === tournamentBuyIn.id)
    ).head
  }
  
  def updateNodeMetrics(nodeId: UUID, metrics: thrift.metrics.Node) {
    update(nodes)(node =>
      where(node.id === nodeId)
      set(
          node.totalConnectionsCount  := metrics.totalConnections,
          node.playerConnectionsCount := metrics.playerConnections,
          node.offlinePlayersCount    := metrics.offlinePlayers,
          node.connectsRate           := metrics.connects.rate15.get,
          node.disconnectsRate        := metrics.disconnects.rate15.get,
          node.messagesReceivedRate   := metrics.messagesReceived.rate15.get
        )
    )
  }
  
  // FIXME copypaste
  def getRooms(nodeId: UUID) = join(rooms, games.leftOuter, mixes.leftOuter, stakes)((room, game, mix, stake) =>
    where(room.nodeId === nodeId)
    select((room, game, mix, stake))
    on(room.gameId === game.map(_.id), room.mixId === mix.map(_.id), room.stakeId === stake.id)
  )
  
  // FIXME copypaste
  def getRoom(id: UUID): Tuple4[Room, Option[Game], Option[Mix], Stake] =
    join(rooms, games.leftOuter, mixes.leftOuter, stakes)((room, game, mix, stake) =>
      where(room.id === id)
      select((room, game, mix, stake))
      on(room.gameId === game.map(_.id), room.mixId === mix.map(_.id), room.stakeId === stake.id)
    ).head
  
  def createSeat(s: Seat) = inTransaction {
//    val c =
//      from(seats)((seat) =>
//        where(seat.roomId === s.roomId and seat.playerId === s.playerId)
//        compute(count(seat.id))
//      )
//    if ((c:Long) == 0)
    deleteSeat(s.roomId, s.playerId)
    seats.insert(s)
  }
  
  def deleteSeat(roomId: UUID, player: UUID) = seats.deleteWhere(seat =>
      (seat.roomId === roomId and seat.playerId === player)
    )
    
//  def deleteSeat(roomId: UUID, pos: Int, player: UUID) = seats.deleteWhere(seat =>
//      (seat.roomId === roomId and seat.pos === pos and seat.playerId === player)
//    )
  
  def updateRoomMetrics(roomId: UUID, metrics: thrift.metrics.Room) {
    update(rooms)(room =>
      where(room.id === roomId)
      set(
          room.playersCount     := metrics.players,
          room.waitingCount     := metrics.waiting,
          room.watchingCount    := metrics.watching,
          room.playsRate        := metrics.plays.rate15.get,
          room.average_pot      := metrics.pots.mean,
          room.playersPerFlop   := metrics.playersPerFlop.mean
        )
    )
  }
    
  def updateRoomState(id: UUID, state: String) =
    update(rooms)(room =>
      where(room.id === id)
      set(room.state := state)
    )
  
  def updateSeatState(roomId: UUID, pos: Int, player: UUID, state: String) =
    update(seats)(seat =>
      where(seat.roomId === roomId and seat.pos === pos and seat.playerId === player)
      set(seat.state := state)
    )

}
