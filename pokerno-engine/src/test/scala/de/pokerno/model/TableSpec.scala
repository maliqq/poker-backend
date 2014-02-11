package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class TableSpec extends FunSpec with ClassicMatchers {

  describe("Table") {
    it("new") {
      val table = new Table(6)
      table.button.current should equal(0)
    }

    it("add player") {
      val table = new Table(6)
      val player = new Player("1")
      val pos = 0
      val stack = 1000

      table.addPlayer(pos, player, Some(stack))

      val seat: Seat = (table.seats: List[Seat])(0)
      seat.player.get should equal(player)
      seat.amount should equal(stack)
      seat.state should equal(Seat.State.Ready)
    }
  }

}
