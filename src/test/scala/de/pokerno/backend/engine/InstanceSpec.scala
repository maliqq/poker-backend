package de.pokerno.backend.engine

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import akka.testkit.{ TestKit, TestFSMRef }
import akka.actor._

class InstanceSpec(_system: ActorSystem) extends TestKit(_system) with FunSpecLike with ClassicMatchers {
  def this() = this(ActorSystem("InstanceSpec"))

  describe("Instance") {
    //    it("create and close") {
    //      val game = new Game(Game.Texas)
    //      val stake = new Stake(10.0)
    //      val instance = TestFSMRef(new Instance(game, stake))
    //      
    //      instance.stateName should equal(Instance.Created)
    //      
    //      instance ! Instance.Stop
    //      instance.stateName should equal(Instance.Closed)
    //    }
    //    
    //    it("pausing") {
    //      val game = new Game(Game.Texas)
    //      val stake = new Stake(10.0)
    //      val instance = TestFSMRef(new Instance(game, stake))
    //      
    //      instance ! Instance.Start
    //      instance.stateName should equal(Instance.Running)
    //      
    //      instance ! Instance.Pause
    //      instance.stateName should equal(Instance.Paused)
    //      
    //      instance ! Instance.Resume
    //      instance.stateName should equal(Instance.Running)
    //    }
  }
}
