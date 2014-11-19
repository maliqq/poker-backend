package de.pokerno.model

import org.scalatest._
import org.scalatest.Matchers._

import de.pokerno.protocol.thrift

class ThriftConversionsSpec extends FunSpec {
  
  import ThriftConversions._

  describe("Thrift") {
    describe("Stake") {
      it("should convert from thrift to model") {
        val st = thrift.Stake(
            10.0,
            Some(5.0),
            None,
            None,
            30, 40
        )
        val stake = st: Stake
        stake.bigBlind should equal(10.0)
        stake.smallBlind should equal(5.0)
      }
    }
    
    describe("Game") {
      it("should convert from thrift to model") {
        val v = thrift.Variation(
            thrift.VariationType.Game,
            Some(
                thrift.Game(thrift.GameType.Texas, thrift.GameLimit.NoLimit, 10, None)
            )
        )
        val variation = v: Variation
        variation.isMixed should not be(true)
      }
    }
    
    describe("Mix") {
      it("should convert from thrift to model") {
        val v = thrift.Variation(
            thrift.VariationType.Mix,
            None,
            Some(
                thrift.Mix(thrift.MixType.Horse, 8)
            )
        )
        val variation = v: Variation
        variation.isMixed should be(true)
      }
    }
  }
  
}