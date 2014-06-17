package de.pokerno.db

import org.squeryl._
import org.squeryl.annotations.{Row, Column}
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

case class Room(
    var id: String,
    var state: String,
    var name: String,
    @Column("game_id") var gameId: Int,
    @Column("stake_id") var stakeId: Int,
    @Column("players_count") var playersCount: Int,
    @Column("waiting_count") var waitingCount: Int) extends KeyedEntity[String] {
  def this() = this("", "", "", 0, 0, 0, 0)
}

case class Stake(
    var id: Int,
    @Column("big_blind") var bigBlind: Double,
    @Column("small_blind") var smallBlind: Double,
    var ante: Option[Double],
    @Column("buy_in_min") var buyInMin: Double,
    @Column("buy_in_max") var buyInMax: Double) extends KeyedEntity[Int] {
  def this() = this(0, 0, 0, None, 0, 0)
}

case class Game(
    var id: Int,
    var variation: String,
    var limit: String,
    @Column("speed") var speed: Option[Int],
    @Column("table_max") var tableSize: Int) {
  def this() = this(0, "", "", None, 0)
}

object Database extends Schema {
  val rooms = table[Room]("poker_tables")
  val games = table[Game]("poker_games")
  val stakes = table[Stake]("poker_table_stakes")
  
  lazy val roomsWithGamesAndStakes = join(rooms, games.leftOuter, stakes.leftOuter)((room, game, stake) =>
    select((room, game, stake))
    on(room.gameId === game.map(_.id), room.stakeId === stake.map(_.id))
  )
}

object Connection {
  def connect() = {
    Class.forName("org.postgresql.Driver")
    val jdbcConnection = java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/poker_development", "malik", "")
    jdbcConnection.setAutoCommit(false)
    Session.create(jdbcConnection, new PostgreSqlAdapter)
  }
}
