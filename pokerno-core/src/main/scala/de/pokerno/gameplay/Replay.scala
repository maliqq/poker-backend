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
  
  override def preStart {
    log.info("starting replay with gameplay {}", gameplay)
//    gameplay.rotateGame(stageContext)
  }
  
  var firstStreet = true

  override def receive = {
    case Replay.Subscribe(out) =>
      log.info("subscribe")

      e.broker.subscribe(out, "replay-out")
      e.start(t, gameplay.variation, gameplay.stake)

    case join @ rpc.JoinPlayer(pos, player, amount) =>
      log.info("got: {}", join)

      t.addPlayer(pos, player, Some(amount))
      e.joinTable((player, pos), amount)

    case s @ rpc.ShowCards(cards, player, muck) =>
      
      log.debug("got: {}", s)
      
      e.showCards(t.box(player).get, cards, muck)
    
    case Betting.Stop => // идем до шоудауна
      log.info("streets done")
      gameplay.showdown()
      context.stop(self)

    case a @ Replay.StreetActions(street, actions, speed) =>
      
      if (firstStreet) {
        gameplay.prepareSeats(stageContext)
        firstStreet = false // FIXME
      }
      
      log.debug("got: {}", a)
      
      if (streets.head == street) {
        // нужный стрит
        streets = streets.drop(1)
        
        val options = streetOptions(street)
        
        log.info("|--- street: {} options: {}", street, options)

        /**
         * DEALING
         * */
        options.dealing.map { dealOptions =>
          log.info("[dealing] started")
          
          val dealActions = actions.filter { action =>
            action match {
              case a: rpc.DealCards =>
                (a.getType: DealCards.Value) == dealOptions.dealType 
              case _ => false
            }
          }.asInstanceOf[List[rpc.DealCards]]
          
          dealing(dealActions, dealOptions)
          
          log.info("[dealing] done")
        }
        
        /**
         * BRING IN
         * */
        if (options.bringIn) {
          log.info("[bring-in] started")
          gameplay.bringIn(stageContext)
          log.info("[bring-in] done")
        }
        
        /**
         * BIG BETS
         * */
        if (options.bigBets) {
          log.info("[big-bets] handled")
          gameplay.round.bigBets = true // TODO notify
        }
        
        /**
         * BETTING
         * */
        if (options.betting) {
          log.info("[betting] started")
          
          val betActions = actions.filter { action =>
            action.isInstanceOf[rpc.AddBet]
          }.asInstanceOf[List[rpc.AddBet]]
          
          betting(betActions)
          
          log.info("[betting] done")
        }
        
        /**
         * DISCARDING
         * */
      }
      
    case x =>
      log.warning("unandled: {}", x)
  }

  override def postStop {
    log.info("actor stopped!")
    e.playStop()
  }
  
}
