package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._
import de.pokerno.data.pokerdb.thrift

object Node {

  import de.pokerno.data.pokerdb.PokerDB._
  
  def getRooms(nodeId: UUID) = join(rooms, games.leftOuter, mixes.leftOuter, stakes)((room, game, mix, stake) =>
    where(room.nodeId === nodeId)
    select((room, game, mix, stake))
    on(room.gameId === game.map(_.id), room.mixId === mix.map(_.id), room.stakeId === stake.id)
  )
  
  def updateMetrics(nodeId: UUID, metrics: thrift.metrics.Node) {
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
}

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
