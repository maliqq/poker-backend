package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class PotSpec extends FunSpec with ClassicMatchers {
  describe("Pot") {
    implicit def string2player(s: String) = new Player(s)

    it("split pot 1") {
      val pot = new SidePot(0, Some(100))
      val p = new Player("P")
      val left = pot.add(p, 120)
      left should equal(20)
      pot.isMember(p) should be(true)
      pot.total should equal(100)
    }

    it("split pot 2") {
      val pot = new SidePot(0, Some(100))
      val p = new Player("P")
      pot.add(p, 100) should equal(0)
      pot.isMember(p) should be(true)
      pot.total should equal(100)
    }

    it("pot 1") {
      val pot = new SidePot
      val p = new Player("P")
      pot.add(p, 100) should equal(0)
      pot.isMember(p) should be(true)
      pot.total should equal(100)

      pot.add(p, 100) should equal(0)
      pot.total should equal(200)
    }

    it("split") {
      val pot = new SidePot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")

      pot.members = Map(
        a -> 10,
        b -> 30,
        c -> 40
      )
      val (_new, _old) = pot.split(a, 10)

      _old.cap.get should equal(20)
      _old.members(a) should equal(20)
      _old.members(b) should equal(20)
      _old.members(c) should equal(20)

      _new.cap.isDefined should be(false)
      _new.members(b) should equal(10)
      _new.members(c) should equal(20)
      _new.isMember(a) should be(false)
    }

    it("all in - 2 players, 1 allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      pot.main.total should equal(40)
      pot.main.isMember(a) should be(true)
      pot.main.isMember(b) should be(false)
      pot.side.head.total should equal(120)
      pot.side.head.isMember(a) should be(true)
      pot.side.head.isMember(b) should be(true)
    }

    it("all in - three players, 1 allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      pot.add(a, 100) should equal(0)
      pot.add(b, 100) should equal(0)
      pot.add(c, 60, true) should equal(0)
      pot.main.total should equal(80)
      pot.main.isMember(a) should be(true)
      pot.main.isMember(b) should be(true)
      pot.main.isMember(c) should be(false)
      pot.side.head.total should equal(180)
      pot.side.head.isMember(a) should be(true)
      pot.side.head.isMember(b) should be(true)
      pot.side.head.isMember(c) should be(true)
    }

    it("all in - three players, middle player allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")

      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      pot.add(c, 100) should equal(0)

      pot.main.total should equal(80)

      pot.main.isMember(a) should be(true)
      pot.main.isMember(b) should be(false)
      pot.main.isMember(c) should be(true)

      pot.side.head.total should equal(180)
      pot.side.head.isMember(a) should be(true)
      pot.side.head.isMember(b) should be(true)
      pot.side.head.isMember(c) should be(true)
    }

    it("all in - 4 players, 2 allins") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")
      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      pot.add(c, 100) should equal(0)
      pot.add(d, 500) should equal(0)
      pot.add(a, 250, true) should equal(0)
      pot.add(c, 400) should equal(0)

      val side1 = pot.side.head
      side1.total should equal(240)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)

      val side2 = pot.side(1)
      side2.total should equal(870)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(false)
      side2.isMember(c) should be(true)
      side2.isMember(d) should be(true)

      val side3 = pot.main
      side3.total should equal(300)
      side3.isMember(a) should be(false)
      side3.isMember(b) should be(false)
      side3.isMember(c) should be(true)
      side3.isMember(d) should be(true)
    }

    it("all in - 4 players, 1 allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 10) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 7, true) should equal(0)
      pot.add(d, 20) should equal(0)
      pot.add(a, 10) should equal(0)
      pot.add(b, 20) should equal(0)
      pot.add(d, 10) should equal(0)

      val side1 = pot.side.last
      side1.total should equal(28)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)

      val side2 = pot.main
      side2.total should equal(59)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(false)
      side2.isMember(d) should be(true)
    }

    it("all in - 4 players, 2 allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 10) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 7, true) should equal(0)
      pot.add(d, 20) should equal(0)
      pot.add(a, 2, true) should equal(0)
      pot.add(b, 20) should equal(0)
      pot.add(d, 10) should equal(0)

      val side1 = pot.side.head
      side1.total should equal(28)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)

      val side2 = pot.side(1)
      side2.total should equal(15)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(false)
      side2.isMember(d) should be(true)

      val side3 = pot.main
      side3.total should equal(36)
      side3.isMember(a) should be(false)
      side3.isMember(b) should be(true)
      side3.isMember(c) should be(false)
      side3.isMember(d) should be(true)
    }

    it("all in - 4 players, 2 allins, 1 round") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 5, true) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 8, true) should equal(0)
      pot.add(d, 10) should equal(0)

      val side1 = pot.side.head
      side1.total should equal(20)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)

      val side2 = pot.side(1)
      side2.total should equal(9)
      side2.isMember(a) should be(false)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(true)
      side2.isMember(d) should be(true)

      val side3 = pot.main
      side3.total should equal(4)
      side3.isMember(a) should be(false)
      side3.isMember(b) should be(true)
      side3.isMember(c) should be(false)
      side3.isMember(d) should be(true)
    }

    it("all in - 4 players, 1 allin, 1 round") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 10) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 7, true) should equal(0)
      pot.add(d, 10) should equal(0)

      val side1 = pot.side.head
      side1.total should equal(28)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)

      val side2 = pot.main
      side2.total should equal(9)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(false)
      side2.isMember(d) should be(true)
    }

    it("all in - 4 players, 1 allin raise") {
      val pot = new Pot

      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 10) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 17, true) should equal(0)
      pot.add(d, 17) should equal(0)
      pot.add(a, 7) should equal(0)
      pot.add(b, 7) should equal(0)

      val side1 = pot.side.head
      side1.total should equal(68)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)

      val side2 = pot.main
      side2.total should equal(0)
    }

    it("all in - 4 players, 1 allin raise, 1 allin call") {
      val pot = new Pot

      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 17, true) should equal(0)
      pot.add(b, 7, true) should equal(0)
      pot.add(c, 17) should equal(0)
      pot.add(d, 17) should equal(0)

      val side1 = pot.side.head
      side1.total should equal(28)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)

      val side2 = pot.side.last
      side2.total should equal(30)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(false)
      side2.isMember(c) should be(true)
      side2.isMember(d) should be(true)
    }

    it("all in - 4 players, 1 allin raise, 1 allin call, 1 allin re-raise") {
      val pot = new Pot

      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 17, true) should equal(0)
      pot.add(b, 7, true) should equal(0)
      pot.add(c, 34, true) should equal(0)
      pot.add(d, 34) should equal(0)

      pot.total should equal(92)
    }

    it("all in - 4 players, 1 allin raise, 1 allin call, raise, raise under allin") {
      val pot = new Pot

      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      val d = new Player("D")

      pot.add(a, 6, true) should equal(0)
      pot.add(b, 5, true) should equal(0)
      pot.add(c, 12) should equal(0)
      pot.add(d, 68) should equal(0)
      pot.add(c, 6, true) should equal(0)

      pot.total should equal(97)
    }

    it("total") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      pot.add(a, 20)
      pot.add(b, 10)
      pot.total should equal(30)
    }
  }
}
