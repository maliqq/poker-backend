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
    it("rebalance normalized shootout") {
      //val t = 16
      val t = 4
      for (roundsNum <- (2 to t)) {
        val n = Math.pow(2, roundsNum).intValue
        val m = Math.pow(2, roundsNum - 1).intValue
        
        val shootout = new Shootout2{
          def entrantsCount = n
        }
        
        shootout.tablesCount should equal(m)
        shootout.roundsNum should equal(roundsNum)
        
        val (map, _) = shootout.rebalance
        map.size should equal(m)
      }
    }
    
    it("rebalance with waiting next level") {
      val shootout = new Shootout2{
        def entrantsCount = 13
      }
      val (map, waiting) = shootout.rebalance
      
      waiting.size should equal(shootout.tablesCount*2-shootout.entrantsCount)
    }
  }
  
  describe("Double shootout - 9max") {
    it("rebalance normalized shootout") {
      val shootout = new Shootout9 {
        def entrantsCount = 81
      }
      shootout.tablesCount should equal(9)
      shootout.roundsNum should equal(2)
      
      val (map, _) = shootout.rebalance
      //println(map)
      map.size should equal(9)
      map.forall { _.size == 9 } should be(true)
    }
    
    it("rebalance with incomplete tables") {
      //for (i <- (18 to 80)) {
        val i = 20
        val shootout = new Shootout9 {
          def entrantsCount = i
        }
        shootout.tablesCount should equal(9)
        shootout.roundsNum should equal(2)
        
        val (map, _) = shootout.rebalance
        println(map)
        
        map.size should equal(9)
        map.filter { _.size == 9 }.size should equal(if (i < 72) 0 else i % 9)
      //}
    }
  }
  
  describe("Double shootout - 10max") {
    it("rebalance normalized shootout") {
      val shootout = new Shootout10 {
        def entrantsCount = 100
      }
      
      shootout.tablesCount should equal(10)
      shootout.roundsNum should equal(2)
      
      val (map, _) = shootout.rebalance
      //println(map)
      map.size should equal(10)
      map.forall { _.size == 10 } should equal(true)
    }
    
    it("rebalance with incomplete tables") {
      val shootout = new Shootout10{
        def entrantsCount = 98 
      }
      
      shootout.tablesCount should equal(10)
      shootout.roundsNum should equal(2)
      
      val (map, _) = shootout.rebalance
      //println(map)
      map.size should equal(10)
      map.filter { _.size == 10 }.size should equal(8)
    }
  }
  
  describe("Triple shootout") {
    it("rebalance normalized shootout") {
      val shootout = new Shootout10 {
        def entrantsCount = 1000
      }
      shootout.tablesCount should equal(100)
      shootout.roundsNum should equal(3)
      
      val (map, _) = shootout.rebalance
      //println(map)
      map.size should equal(100)
      map.forall { _.size == 10 } should be(true)
    }
  }

}
