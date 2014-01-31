package de.pokerno.gameplay

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.protocol.{msg => message}
import de.pokerno.protocol.{rpc, wire}
import wire.Conversions._
import de.pokerno.protocol.Conversions._
import akka.actor.Actor

object Betting {

  // start new round
  case object Start
  // require bet
  case class Require(amount: Decimal, limit: Game.Limit)
  // force bet
  case class Force(amount: Decimal)
  case class Add(bet: Bet)

  // go to next seat
  case object NextTurn
  // stop current deal
  case object Stop
  // betting done - wait for next street to occur
  case object Done
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

  trait DealContext { 
    deal: Deal =>
  
    def handleBetting: Receive = {
      case Betting.Add(bet) ⇒
        log.info("[betting] add {}", bet)
        gameplay.round.addBet(bet)
        nextTurn()
  
      case Betting.NextTurn ⇒
        log.info("[betting] next turn")
        nextTurn()
  
      case Betting.Stop ⇒
        log.info("[betting] stop")
        context.become(handleStreets)
        self ! Streets.Done
        
      case Betting.Timeout ⇒
        log.info("[betting] timeout")
        nextTurn()
  
      case Betting.Done ⇒
        log.info("[betting] done")
        gameplay.round.complete
        context.become(handleStreets)
        streets(stageContext)
  
      case Betting.BigBets ⇒
        log.info("[betting] big bets")
        gameplay.round.bigBets = true
    }
    
    protected def nextTurn() {
      gameplay.round.move
      gameplay.round.seats filter (_._1 inPlay) foreach {
        case (seat, pos) ⇒
          if (!seat.isCalled(gameplay.round.call)) seat.playing
      }
  
      if (gameplay.round.seats.filter(_._1 inPot).size < 2)
        self ! Betting.Stop
      else {
        val active = gameplay.round.seats filter (_._1 isPlaying)
  
        if (active.size == 0)
          self ! Betting.Done
        else
          gameplay.round requireBet active.head
      }
    }
  }
  
  trait ReplayContext {
    replay: Replay =>
      
      def firstStreet: Boolean = false
      
      def nextTurn(bets: List[rpc.AddBet]) {
        val round = gameplay.round
        val gameOptions = gameplay.game.options
        val stake = gameplay.stake
        val table = gameplay.table
        
        val (forcedBets, activeBets) = bets.span { addBet =>
          (addBet.bet.getType: Bet.Value).isInstanceOf[Bet.ForcedBet]
        }
        
        val anteBets = forcedBets.filter { addBet =>
          (addBet.bet.getType: Bet.Value) == Bet.Ante
        }
        
        val postAnte = firstStreet && (gameOptions.hasAnte || stake.ante.isDefined)
        
        // пассивные ставки игроков - анте
        if (postAnte) {
          if (activeBets.isEmpty) {
            anteBets.foreach { anteBet =>
              val player: Player = if (anteBet.player != null)
                anteBet.player
              else {
                val acting = round.acting
                acting._1.player.get
              }
              
              val seatOption = table.seat(anteBet.player)
              if (seatOption.isDefined) {
                val (seat, pos) = seatOption.get
                // process ante bet if seat is active
                if (seat.isActive) round.forceBet((seat, pos), Bet.Ante)
              }
            }
          } else {
            val postingAnte = round.seats.filter(_._1.isActive)
            postingAnte.foreach { case (seat, pos) =>
              round.forceBet((seat, pos), Bet.Ante)
            }
            round.complete
          }
        }
        
        // пассивные ставки игроков - блайнды
        val postBlinds = firstStreet && gameOptions.hasBlinds
        
        val active = round.seats.filter(_._1.isActive)
        if (postBlinds && active.size >= 2) {
          val sbOption = forcedBets.find { addBet =>
            (addBet.bet.getType: Bet.Value) == Bet.SmallBlind
          }
          
          val bbOption = forcedBets.find { addBet =>
            (addBet.bet.getType: Bet.Value) == Bet.BigBlind
          }
          
          val List(sb, bb, _*) = active
        }
        
        // активные ставки игроков
        if (!activeBets.isEmpty) {
          val active = round.seats.filter(_._1.isActive)
        }
      }
  }

}
