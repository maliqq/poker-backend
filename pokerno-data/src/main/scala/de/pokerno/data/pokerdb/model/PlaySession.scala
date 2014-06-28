package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

object PlaySession {
  import de.pokerno.data.pokerdb.PokerDB._
  
  def create(roomId: UUID, playerId: UUID, pos: Int, stack: Double) = inTransaction {
//    val c =
//      from(seats)((seat) =>
//        where(seat.roomId === s.roomId and seat.playerId === s.playerId)
//        compute(count(seat.id))
//      )
//    if ((c:Long) == 0)
    end(roomId, playerId) // ended previous session
    sessions.insert(new PlaySession(roomId, playerId, pos, stack))
  }
  
  def end(roomId: UUID, playerId: UUID) = update(sessions)(session =>
      where(session.roomId === roomId and session.playerId === playerId)
      set(
          session.ended := now()
        )
    )
    
//  def deleteSeat(roomId: UUID, pos: Int, player: UUID) = seats.deleteWhere(seat =>
//      (seat.roomId === roomId and seat.pos === pos and seat.playerId === player)
//    )
//    def updateState(roomId: UUID, pos: Int, player: UUID, state: String) =
//      update(seats)(seat =>
//        where(seat.roomId === roomId and seat.pos === pos and seat.playerId === player)
//        set(seat.state := state)
//      )
}

sealed case class PlaySession(
    @Column("room_id") var roomId: UUID,
    @Column("player_id") var playerId: UUID,
    var pos: Int,
    @Column("starting_stack") var startingStack: Double,
    @Column("started_at") var started: Timestamp = now()
) extends KeyedEntity[Long] {
  var id: Long = 0
  @Column("ended_at") var ended: Timestamp = null
  def this() = this(null, null, 0, 0, null)
}
