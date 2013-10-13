package test.pokerno.backend.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.model.{ Table, Seat, Player }

class TableSpec extends FunSpec with ClassicMatchers {

  describe("Table") {
    it("new") {
      val table = new Table(6)
      table.button should equal(0)
    }

    it("add player") {
      val table = new Table(6)
      val player = new Player("1")
      val pos = 0
      val stack = 1000

      table.addPlayer(player, pos, Some(stack))

      val seat: Seat = table.seats(0)
      seat.player.get should equal(player)
      seat.amount should equal(stack)
      seat.state should equal(Seat.Ready)
    }
  }

}
