package de.pokerno.gameplay

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import akka.testkit.TestActorRef

import de.pokerno.model._

class StreetsSpec(_system: ActorSystem) extends TestKit(_system) with FunSpecLike with ClassicMatchers {
  def this() = this(ActorSystem("test"))
  
  val variation = new Game(Game.Texas, Some(Game.NoLimit), Some(10))
  val events = new Events
  val stake = new Stake(100)
  val table = new Table(variation.tableSize)
  
  val gameplay = new Context(
      events,
      variation,
      stake,
      table
  )
  
  val context = new StageContext(gameplay, system.actorOf(Props(classOf[DealActor], gameplay)))
  
  describe("Streets") {
    it("") {
      Streets(context)
    }
  }
}
