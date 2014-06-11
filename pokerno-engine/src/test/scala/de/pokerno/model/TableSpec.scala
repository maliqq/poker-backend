package de.pokerno.model

import org.scalatest._
import org.scalatest.Matchers._

class TableSpec extends FunSpec {

  describe("Table") {
    it("new") {
      val table = new Table(6)
      table.button.current should equal(0)
    }

    it("toString") {
      val table = new Table(6)
      table.toString() should equal("")
    }

    it("add player") {
      val table = new Table(6)
      val player = new Player("1")
      val pos = 0
      val stack = 1000

      table.takeSeat(pos, player, Some(stack))

      val seat: Seat = table.seats(0)
      seat.player.get should equal(player)
      seat.stack should equal(stack)
      seat.state should equal(Seat.State.Ready)
    }
  }

}
