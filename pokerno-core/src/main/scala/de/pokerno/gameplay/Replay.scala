package de.pokerno.gameplay

import de.pokerno.protocol
import de.pokerno.protocol.{msg, rpc, wire}
import protocol.Conversions._
import protocol.wire.Conversions._
import protocol.msg.Conversions._
import de.pokerno.model._
import akka.actor.{Actor, Props, ActorRef, ActorLogging}

object Replay {
  case class Subscribe(out: ActorRef)
  case class StreetActions(street: Street.Value, actions: List[rpc.Request], speed: Int)
}

class Replay(val gameplay: Context) extends Actor
      with ActorLogging
      with Dealing.ReplayContext
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
//
//    case addBet @ rpc.AddBet(player, bet) =>
//      log.info("got: {}", addBet)
//
//      if (bet.isForced) {
//
//        val (seat, pos) = t.seat(player).get
//        gameplay.round.forceBet((seat, pos), bet.betType.asInstanceOf[Bet.ForcedBet])
//
//      } else gameplay.round.addBet(bet)
//      
//      e.addBet(t.box(player).get, bet)

    case s @ rpc.ShowCards(cards, player, muck) =>
      
      log.debug("got: {}", s)
      
      e.showCards(t.box(player).get, cards, muck)
//
//    case d @ rpc.DealCards(_type, player, cards, cardsNum) =>
//      
//      log.debug("got: {}", d)
//      
//      dealCards(_type, player, cards, cardsNum)
//
//    case Streets.Next =>
//      log.debug("streets next")
//
//      gameplay.prepareSeats(stageContext)
//      streets(stageContext)
//      
//    case Streets.Done =>
//      log.debug("showdown")
//
//      gameplay.showdown
//      context.stop(self)
    
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
          val dealer = gameplay.dealer
          
          val dealActions = actions.filter { action =>
            action match {
              case a: rpc.DealCards =>
                (a.getType: DealCards.Value) == dealOptions.dealType 
              case _ => false
            }
          }.asInstanceOf[List[rpc.DealCards]]
          
          val _type = dealOptions.dealType
          
          _type match {
          case DealCards.Board =>
            
            val dealBoard = dealActions.head.asInstanceOf[rpc.DealCards]
            dealCards(_type,
                cards = if (dealBoard.cards != null) Some(dealBoard.cards)
                        else None,
                cardsNum = dealOptions.cardsNum
            )
            
          case DealCards.Door | DealCards.Hole =>
            
            val dealActionsByPlayer = collection.mutable.HashMap[Player, rpc.DealCards]()
            dealActions.foreach { action =>
              dealActionsByPlayer(action.player) = action
            }
            
            t.seatsAsList.zipWithIndex filter (_._1 isActive) foreach {
              case (seat, pos) ⇒
                val player = seat.player.get
                if (dealActionsByPlayer.contains(player)) {
                  val dealPocket = dealActionsByPlayer(player)
                  
                  dealCards(_type,
                      player = Some(player),
                      cards = if (dealPocket.cards != null) Some(dealPocket.cards)
                              else None,
                      cardsNum = if (dealPocket.cardsNum != null) Some(dealPocket.cardsNum)
                                 else None
                      )
                } else {
                  val pocketSize = gameOptions.pocketSize
                  val n = dealOptions.cardsNum.getOrElse(pocketSize)
                  val cards = dealer.dealPocket(n, player)
                  e.dealCards(_type, cards, Some((seat.player.get, pos)))
                }
            }
          }
          
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
  }
  
}
