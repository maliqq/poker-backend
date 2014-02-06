package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class PotSpec extends FunSpec with ClassicMatchers {
  describe("Pot") {
    implicit def string2player(s: String) = new Player(s)
    
    it("split pot 1") {
      val pot = new SidePot(Some(100))
      val left = pot.add("P", 120)
      left should equal(20)
      pot.isMember("P") should be(true)
      pot.total should equal(100)
    }
    
    it("split pot 2") {
      val pot = new SidePot(Some(100))
      pot.add("P", 100) should equal(0)
      pot.isMember("P") should be(true)
      pot.total should equal(100)
    }
    
    it("pot 1") {
      val pot = new SidePot
      pot.add("P", 100) should equal(0)
      pot.isMember("P") should be(true)
      pot.total should equal(100)
      
      pot.add("P", 100) should equal(0)
      pot.total should equal(200)
    }
    
    it("split") {
      val pot = new SidePot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      
      pot.members = Map(
          a -> 10,
          b -> 30,
          c -> 40
      )
      val (_new, _old) = pot.split(a, 10)
      
      _new.cap.get should equal(20)
      _new.members(a) should equal(20)
      _new.members(b) should equal(20)
      _new.members(c) should equal(20)
      
      _old.cap.isDefined should be(false)
      _old.members(b) should equal(10)
      _old.members(c) should equal(20)
      _old.isMember(a) should be(false)
    }
    
    it("all in - 2 players, 1 allin") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      pot.current.total should equal(40)
      pot.current.isMember(a) should be(true)
      pot.current.isMember(b) should be(false)
      pot.active.head.total should equal(120)
      pot.active.head.isMember(a) should be(true)
      pot.active.head.isMember(b) should be(true)
    }
    
    it("all in - three players, 1 allin") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      pot.add(a, 100) should equal(0)
      pot.add(b, 100) should equal(0)
      pot.add(c, 60, true) should equal(0)
      pot.current.total should equal(80)
      pot.current.isMember(a) should be(true)
      pot.current.isMember(b) should be(true)
      pot.current.isMember(c) should be(false)
      pot.active.head.total should equal(180)
      pot.active.head.isMember(a) should be(true)
      pot.active.head.isMember(b) should be(true)
      pot.active.head.isMember(c) should be(true)
    }
    
    it("all in - three players, middle player allin") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      
      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      pot.add(c, 100) should equal(0)
      
      pot.current.total should equal(80)
      
      pot.current.isMember(a) should be(true)
      pot.current.isMember(b) should be(false)
      pot.current.isMember(c) should be(true)
      
      pot.active.head.total should equal(180)
      pot.active.head.isMember(a) should be(true)
      pot.active.head.isMember(b) should be(true)
      pot.active.head.isMember(c) should be(true)
    }
    
    it("all in - 4 players, 2 allins") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      val d = Player("D")
      pot.add(a, 100) should equal(0)
      pot.add(b, 60, true) should equal(0)
      pot.add(c, 100) should equal(0)
      pot.add(d, 500) should equal(0)
      pot.add(a, 250, true) should equal(0)
      pot.add(c, 400) should equal(0)
      
      val side1 = pot.active.head
      side1.total should equal(240)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)
      
      val side2 = pot.active(1)
      side2.total should equal(870)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(false)
      side2.isMember(c) should be(true)
      side2.isMember(d) should be(true)
      
      val side3 = pot.current
      side3.total should equal(300)
      side2.isMember(a) should be(false)
      side2.isMember(b) should be(false)
      side2.isMember(c) should be(true)
      side2.isMember(d) should be(true)
    }
    
    it("all in - 4 players, 1 allin") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      val d = Player("D")
      
      pot.add(a, 10) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 7, true) should equal(0)
      pot.add(d, 20) should equal(0)
      pot.add(a, 10) should equal(0)
      pot.add(b, 20) should equal(0)
      pot.add(d, 10) should equal(0)
      
      val side1 = pot.active.last
      side1.total should equal(28)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)
      
      val side2 = pot.current
      side2.total should equal(59)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(false)
      side2.isMember(d) should be(true)
    }
    
    it("all in - 4 players, 2 allin") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      val d = Player("D")
      
      pot.add(a, 10) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 7, true) should equal(0)
      pot.add(d, 20) should equal(0)
      pot.add(a, 2, true) should equal(0)
      pot.add(b, 20) should equal(0)
      pot.add(d, 10) should equal(0)
      
      val side1 = pot.active.head
      side1.total should equal(28)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)
      
      val side2 = pot.active(1)
      side2.total should equal(15)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(false)
      side2.isMember(d) should be(true)
      
      val side3 = pot.current
      side3.total should equal(36)
      side3.isMember(a) should be(false)
      side3.isMember(b) should be(true)
      side3.isMember(c) should be(false)
      side3.isMember(d) should be(true)
    }
    
    it("all in - 4 players, 2 allins, 1 round") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      val d = Player("D")
      
      pot.add(a, 5, true) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 8, true) should equal(0)
      pot.add(d, 10) should equal(0)
      
      val side1 = pot.active.head
      side1.total should equal(20)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)
      
      val side2 = pot.active(1)
      side2.total should equal(9)
      side2.isMember(a) should be(false)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(false)
      side2.isMember(d) should be(true)
      
      val side3 = pot.current
      side3.total should equal(4)
      side3.isMember(a) should be(false)
      side3.isMember(b) should be(true)
      side3.isMember(c) should be(false)
      side3.isMember(d) should be(true)
    }
    
    it("all in - 4 players, 1 allin, 1 round") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      val c = Player("C")
      val d = Player("D")
      
      pot.add(a, 10) should equal(0)
      pot.add(b, 10) should equal(0)
      pot.add(c, 7, true) should equal(0)
      pot.add(d, 10) should equal(0)
      
      val side1 = pot.active.last
      side1.total should equal(28)
      side1.isMember(a) should be(true)
      side1.isMember(b) should be(true)
      side1.isMember(c) should be(true)
      side1.isMember(d) should be(true)
      
      val side2 = pot.current
      side2.total should equal(9)
      side2.isMember(a) should be(true)
      side2.isMember(b) should be(true)
      side2.isMember(c) should be(false)
      side2.isMember(d) should be(true)
    }
    
    it("total") {
      val pot = new Pot
      val a = Player("A")
      val b = Player("B")
      pot.add(a, 20)
      pot.add(b, 10)
      pot.total should equal(30)
    }
  }
}
