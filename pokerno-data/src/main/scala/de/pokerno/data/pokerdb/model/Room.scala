package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._
import de.pokerno.data.pokerdb.thrift

object Room {

  import de.pokerno.data.pokerdb.PokerDB._
  
  def get(id: UUID): Tuple4[Room, Option[Game], Option[Mix], Stake] =
    join(rooms, games.leftOuter, mixes.leftOuter, stakes)((room, game, mix, stake) =>
      where(room.id === id)
      select((room, game, mix, stake))
      on(room.gameId === game.map(_.id), room.mixId === mix.map(_.id), room.stakeId === stake.id)
    ).head
    
  def getStake(roomId: UUID): Stake =
    join(rooms, stakes)((room, stake) =>
      where(room.id === roomId)
      select(stake)
      on(room.stakeId === stake.id)
    ).head
    
  def updateMetrics(roomId: UUID, metrics: thrift.metrics.Room) {
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
    
  def updateState(id: UUID, state: String) =
    update(rooms)(room =>
      where(room.id === id)
      set(room.state := state)
    )
  
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
