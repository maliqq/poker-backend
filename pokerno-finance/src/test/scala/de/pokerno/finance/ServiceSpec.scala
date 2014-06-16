package de.pokerno.finance

import org.scalatest._
import org.scalatest.Matchers._
import math.{BigDecimal => Decimal}

class ServiceSpec extends FunSpec {
  describe("Service") {
    it("available") {
      val service = new Service
      val player = "A"
        
      service.available(player)() should equal(10000)
      service.inPlay(player)() should equal(0)
      service.total(player)() should equal(10000)
      
      an[thrift.Error] should be thrownBy service.withdraw(player, 10001)()
    }
    
    it("withdraws") {
      val service = new Service
      val player = "A"
      
      service.withdraw(player, 100)()
      service.available(player)() should equal(9900)
      service.inPlay(player)() should equal(100)
      
      service.deposit(player, 99)()
      service.available(player)() should equal(9999)
    }
    
    it("withdraw to limit") {
      val service = new Service
      val player = "A"
      
      service.withdraw(player, 9998)()
      service.available(player)() should equal(2)
      service.withdraw(player, 1)()
      service.withdraw(player, 1)()
      service.available(player)() should equal(0)
      an[thrift.Error] should be thrownBy service.withdraw(player, 1)()
    }
    
    it("refills") {
      val service = new Service
      val player = "A"
        
      service.withdraw(player, 10000)()
      an[thrift.Error] should be thrownBy service.withdraw(player, 1)()
    }
  }
}
