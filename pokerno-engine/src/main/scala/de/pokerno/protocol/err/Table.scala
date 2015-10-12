package de.pokerno.protocol.err

object Table {
  object Seat {
    class Taken(
      pos: Int,
      player: Player
    ) extends Err("table.seat.taken") {
      val payload = Map[String, Any](
        "pos" -> pos,
        "player" -> player
      )
      val message = f"seat pos=[$pos] is already taken by player=[$player]"
    }
  }

  object Player {
    class AlreadyJoined(
      pos: Int,
      player: Player
      ) extends Err("table.seat.already_joined") {
      val payload = Map[String, Any](
        "pos" -> pos,
        "player" -> player
      )
      def message = f"player=[$player] has already joined to this table"
    }
  }
}
