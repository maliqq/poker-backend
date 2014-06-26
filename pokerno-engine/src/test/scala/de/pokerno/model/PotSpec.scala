package de.pokerno.model

import org.scalatest._
import org.scalatest.Matchers._

class PotSpec extends FunSpec {
  describe("Pot") {
    implicit def string2player(s: String) = new Player(s)

    it("split pot 1") {
      val pot = new SidePot(Some(100))
      val p = new Player("P")
      val left = pot.add(p, 120)
      left should equal(20)
      pot.contains(p) should be(true)
      pot.total should equal(100)
    }

    it("split pot 2") {
      val pot = new SidePot(Some(100))
      val p = new Player("P")
      pot.add(p, 100) should equal(0)
      pot.contains(p) should be(true)
      pot.total should equal(100)
    }

    it("pot 1") {
      val pot = new SidePot
      val p = new Player("P")
      pot.add(p, 100) should equal(0)
      pot.contains(p) should be(true)
      pot.total should equal(100)

      pot.add(p, 100) should equal(0)
      pot.total should equal(200)
    }

    it("split") {
      val pot = new SidePot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")

      pot.members.put(a, 10)
      pot.members.put(b, 30)
      pot.members.put(c, 40)
      val _new = pot.split(a, 10).get

      pot.cap.isDefined should be(false)
      pot.members(b) should equal(10)
      pot.members(c) should equal(20)
      pot.contains(a) should be(false)
      
      _new.cap.get should equal(20)
      _new.members(a) should equal(20)
      _new.members(b) should equal(20)
      _new.members(c) should equal(20)
    }

    it("all in - 2 players, 1 allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      
      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      
      pot.side.size should equal(1)
      
      pot.main.contains(a) should be(true)
      pot.main.contains(b) should be(false)
      
      pot.side.head.contains(a) should be(true)
      pot.side.head.contains(b) should be(true)
      
      pot.main.total should equal(40)
      pot.side.head.total should equal(120)
    }

    it("all in - three players, 1 allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")
      
      pot.add(a, 100) should equal(0)
      pot.add(b, 100) should equal(0)
      pot.add(c, 60, true) should equal(0)
      
      pot.side.size should equal(1)
      
      pot.main.contains(a) should be(true)
      pot.main.contains(b) should be(true)
      pot.main.contains(c) should be(false)
      
      pot.side.head.contains(a) should be(true)
      pot.side.head.contains(b) should be(true)
      pot.side.head.contains(c) should be(true)
      
      pot.main.total should equal(80)
      pot.side.head.total should equal(180)
    }

    it("all in - three players, middle player allin") {
      val pot = new Pot
      val a = new Player("A")
      val b = new Player("B")
      val c = new Player("C")

      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      pot.add(c, 100) should equal(0)
      
      pot.side.size should equal(1)

      pot.main.contains(a) should be(true)
      pot.main.contains(b) should be(false)
      pot.main.contains(c) should be(true)

      pot.side.head.contains(a) should be(true)
      pot.side.head.contains(b) should be(true)
      pot.side.head.contains(c) should be(true)

      pot.main.total should equal(80)
      pot.side.head.total should equal(180)
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
      
      pot.side.size should equal(2)
      
      val side1 = pot.side(1)
      side1.total should equal(240)
      side1.contains(a) should be(true)
      side1.contains(b) should be(true)
      side1.contains(c) should be(true)
      side1.contains(d) should be(true)

      val side2 = pot.side.head
      side2.total should equal(870)
      side2.contains(a) should be(true)
      side2.contains(b) should be(false)
      side2.contains(c) should be(true)
      side2.contains(d) should be(true)

      pot.main.total should equal(300)
      pot.main.contains(a) should be(false)
      pot.main.contains(b) should be(false)
      pot.main.contains(c) should be(true)
      pot.main.contains(d) should be(true)
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
      
      pot.side.size should equal(1)

      val side = pot.side.head
      side.contains(a) should be(true)
      side.contains(b) should be(true)
      side.contains(c) should be(true)
      side.contains(d) should be(true)

      pot.main.contains(a) should be(true)
      pot.main.contains(b) should be(true)
      pot.main.contains(c) should be(false)
      pot.main.contains(d) should be(true)
      
      side.total should equal(28)
      pot.main.total should equal(59)
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
      
      pot.side.size should equal(2)

      val side1 = pot.side(1)
      side1.contains(a) should be(true)
      side1.contains(b) should be(true)
      side1.contains(c) should be(true)
      side1.contains(d) should be(true)

      val side2 = pot.side.head
      side2.contains(a) should be(true)
      side2.contains(b) should be(true)
      side2.contains(c) should be(false)
      side2.contains(d) should be(true)

      val side3 = pot.main
      side3.contains(a) should be(false)
      side3.contains(b) should be(true)
      side3.contains(c) should be(false)
      side3.contains(d) should be(true)

      side1.total should equal(28)
      side2.total should equal(15)
      side3.total should equal(36)
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
      
      pot.side.size should equal(2)
      
      val side1 = pot.side(1)
      side1.contains(a) should be(true)
      side1.contains(b) should be(true)
      side1.contains(c) should be(true)
      side1.contains(d) should be(true)

      val side2 = pot.side.head
      side2.contains(a) should be(false)
      side2.contains(b) should be(true)
      side2.contains(c) should be(true)
      side2.contains(d) should be(true)

      val side3 = pot.main
      side3.contains(a) should be(false)
      side3.contains(b) should be(true)
      side3.contains(c) should be(false)
      side3.contains(d) should be(true)

      side1.total should equal(20)
      side2.total should equal(9)
      side3.total should equal(4)
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
      
      pot.side.size should equal(1)

      val side1 = pot.side.head
      side1.contains(a) should be(true)
      side1.contains(b) should be(true)
      side1.contains(c) should be(true)
      side1.contains(d) should be(true)

      val side2 = pot.main
      side2.contains(a) should be(true)
      side2.contains(b) should be(true)
      side2.contains(c) should be(false)
      side2.contains(d) should be(true)

      side1.total should equal(28)
      side2.total should equal(9)
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
      
      pot.side.size should equal(0)

      pot.main.contains(a) should be(true)
      pot.main.contains(b) should be(true)
      pot.main.contains(c) should be(true)
      pot.main.contains(d) should be(true)

      pot.main.total should equal(68)
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
      
      pot.side.size should equal(1)
      
      val side = pot.side.head
      side.contains(a) should be(true)
      side.contains(b) should be(true)
      side.contains(c) should be(true)
      side.contains(d) should be(true)

      pot.main.contains(a) should be(true)
      pot.main.contains(b) should be(false)
      pot.main.contains(c) should be(true)
      pot.main.contains(d) should be(true)

      side.total should equal(28)
      pot.main.total should equal(30)
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
