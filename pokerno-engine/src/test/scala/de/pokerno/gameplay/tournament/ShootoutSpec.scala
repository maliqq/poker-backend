package de.pokerno.gameplay.tournament

import org.scalatest._
import org.scalatest.Matchers._

abstract class Shootout2 extends Shootout {
  def tableSize = 2
}

abstract class Shootout10 extends Shootout {
  def tableSize = 10
}

abstract class Shootout9 extends Shootout {
  def tableSize = 9
}

class ShootoutSpec extends FunSpec {
  
  describe("Binary shootout") {
    
  }
  
  describe("Double shootout - 9max") {
    it("rebalance normalized shootout") {
      val map = new Shootout9 {
        def entrantsCount = 81
      }.rebalance
      //println(map)
      map.size should equal(9)
      map.forall { _.size == 9 } should equal(true)
    }
    
    it("rebalance with incomplete tables") {
      for (i <- (18 to 80)) {
        val map = new Shootout9 {
          def entrantsCount = i
        }.rebalance
        println(map)
        
        map.size should equal(9)
        map.filter { _.size == 9 }.size should equal(if (i < 72) 0 else i % 9)
      }
    }
  }
  
  describe("Double shootout - 10max") {
    it("rebalance normalized shootout") {
      val map = new Shootout10 {
        def entrantsCount = 100
      }.rebalance
      //println(map)
      map.size should equal(10)
      map.forall { _.size == 10 } should equal(true)
    }
    
    it("rebalance with incomplete tables") {
      val map = new Shootout10{
        def entrantsCount = 98 
      }.rebalance
      println(map)
      map.size should equal(10)
      map.filter { _.size == 10 }.size should equal(8)
    }
  }
  
  describe("Triple shootout") {
    it("") {
      
    }
  }

}
