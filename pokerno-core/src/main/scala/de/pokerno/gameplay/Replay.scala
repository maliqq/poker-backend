package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{msg, rpc, wire}
import protocol.Conversions._
import protocol.wire.Conversions._
//import protocol.msg.Conversions._
import de.pokerno.model._
import akka.actor.{Actor, Props, ActorRef, ActorLogging}

object Replay {
  case class Subscribe(out: ActorRef)
  case class StreetActions(street: Street.Value, actions: List[rpc.Request], speed: Int)
}

class Replay(val gameplay: Context) extends Actor
      with ActorLogging
      with Dealing.ReplayContext
      with Betting.ReplayContext
      with Streets.ReplayContext {
  
  val stageContext = StageContext(gameplay, self)
  
  def e = gameplay.events
  def t = gameplay.table
  
  val gameOptions = gameplay.game.options
  val streetOptions = Streets.Options(gameOptions.group)
  var streets = Street.byGameGroup(gameOptions.group)
  
  import concurrent.duration._
  import de.pokerno.util.ConsoleUtils._
  
  override def preStart {
    info("starting replay with gameplay %s", gameplay)
    //e.playStart()
    //gameplay.rotateGame(stageContext)
  }

  var firstStreet = true

  override def receive = {
    case Replay.Subscribe(out) =>
      e.broker.subscribe(out, "replay-out")
      e.start(t, gameplay.variation, gameplay.stake)

    case join @ rpc.JoinPlayer(pos, player, amount) =>
      debug("got: %s", join)

      t.addPlayer(pos, player, Some(amount))
      e.joinTable((player, pos), amount)

    case s @ rpc.ShowCards(cards, player, muck) =>
      
      debug("got: %s", s)
      
      e.showCards(t.box(player).get, cards, muck)
    
    case Betting.Stop => // идем до шоудауна
      info("streets done")
      gameplay.showdown()
      context.stop(self)

    case a @ Replay.StreetActions(street, actions, speed) =>
      
      if (firstStreet) {
        gameplay.prepareSeats(stageContext)
      }
      
      debug("got: %s", a)
      
      if (streets.head == street) {
        // нужный стрит
        streets = streets.drop(1)
        // notify street started
        e.streetStart(street)
        
        val options = streetOptions(street)
        
        debug(" | street: %s options: %s", street, options)

        /**
         * DEALING
         * */
        options.dealing.map { dealOptions =>
          info("[dealing] started")
          
          val dealActions = actions.filter { action =>
            action match {
              case a: rpc.DealCards =>
                (a.getType: DealCards.Value) == dealOptions.dealType 
              case _ => false
            }
          }.asInstanceOf[List[rpc.DealCards]]
          
          dealing(dealActions, dealOptions, (speed seconds))
          
          info("[dealing] done")
        }
        
        /**
         * BRING IN
         * */
        if (options.bringIn) {
          info("[bring-in] started")
          gameplay.bringIn(stageContext)
          info("[bring-in] done")
        }
        
        /**
         * BIG BETS
         * */
        if (options.bigBets) {
          info("[big-bets] handled")
          gameplay.round.bigBets = true // TODO notify
        }
        
        /**
         * BETTING
         * */
        if (options.betting) {
          info("[betting] started")
          
          val betActions = actions.filter { action =>
            action.isInstanceOf[rpc.AddBet]
          }.asInstanceOf[List[rpc.AddBet]]
          
          betting(betActions, (speed seconds))
          
          info("[betting] done")
        }
        
        /**
         * DISCARDING
         * */
        //
        
        if (firstStreet) firstStreet = false
      }
      
    case x =>
      warn("unandled: %s", x)
  }

  override def postStop {
    e.playStop()
  }
  
}
