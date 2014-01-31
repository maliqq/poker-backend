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
  case class Add(player: Player, bet: Bet)

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
      case Betting.Add(player, bet) ⇒
        val (seat, pos) = gameplay.round.acting
        if (seat.player == player) {
          log.info("[betting] add {}", bet)
          gameplay.round.addBet(bet)
          nextTurn()
        } else
          log.warning("[betting] not a turn of {}", player)
  
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
      val round = gameplay.round
      
      round.move
      round.seats filter (_._1 inPlay) foreach {
        case (seat, pos) ⇒
          if (!seat.isCalled(round.call)) seat.playing()
      }
  
      if (round.seats.filter(_._1 inPot).size < 2)
        self ! Betting.Stop
      else {
        val active = round.seats filter (_._1 isPlaying)
  
        if (active.size == 0)
          self ! Betting.Done
        else
          round requireBet active.head
      }
    }
  }
  
  trait ReplayContext {
    replay: Replay =>
      
      def firstStreet: Boolean = false
      
      def nextTurn(bets: List[rpc.AddBet]) {
        val round = gameplay.round
        
        def active = round.seats.filter(_._1.isActive)
        
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
          }
          round.complete
        }
        
        // пассивные ставки игроков - блайнды
        val postBlinds = firstStreet && gameOptions.hasBlinds
        
        val activeBeforeButtonMove = active
        if (postBlinds && activeBeforeButtonMove.size >= 2) {
          var sb: Option[Tuple2[Seat, Int]] = None
          var bb: Option[Tuple2[Seat, Int]] = None
          
          val sbBetOption = forcedBets.find { addBet =>
            (addBet.bet.getType: Bet.Value) == Bet.SmallBlind
          }
          
          sbBetOption map { sbBet =>
            activeBeforeButtonMove.find { case (seat, pos) =>
              seat.player.isDefined && sbBet.player == seat.player.get.id
            } map { _sb =>
              sb = Some(_sb)
            }
          }
          
          if (sb.isDefined) {
            val (sbPlayer, sbPos) = sb.get
            gameplay.setButton(sbPos - 1) // put button before SB
            
            forcedBets.find { addBet =>
              (addBet.bet.getType: Bet.Value) == Bet.BigBlind
            } map { bbBet =>
              activeBeforeButtonMove.find { case (seat, pos) =>
                val found = seat.player.isDefined && bbBet.player == seat.player.get.id
                
                if (!found && seat.player != sbPlayer)
                  seat.idle() // помечаем все места от SB до BB как неактивные 
                
                found
              } map { _bb =>
                bb = Some(_bb)
              }
            }
            
          } else {
            gameplay.moveButton
            
            // default blind positions
            val List(_sb, _bb, _*) = active
            sb = Some(_sb)
            bb = Some(_bb)
          }
          
          sb.map { sb => round.forceBet(sb, Bet.SmallBlind) }
          bb.map { bb => round.forceBet(bb, Bet.SmallBlind) }
        }
        
        // активные ставки игроков
        if (!activeBets.isEmpty)
          activeBets.takeWhile { addBet =>
            val acting = round.acting
            var done = false
            
            if (acting._1.player.isDefined && acting._1.player.get.id == addBet.player) {
              // FIXME: remove copy&paste
              round.addBet(addBet.bet)
              round.move
              round.seats filter (_._1 inPlay) foreach {
                case (seat, pos) ⇒
                  if (!seat.isCalled(round.call)) seat.playing()
              }
          
              if (round.seats.filter(_._1 inPot).size < 2) {
                self ! Betting.Stop
                done = true
              } else {
                val active = round.seats filter (_._1 isPlaying)
          
                if (active.size == 0) {
                  self ! Betting.Done
                  done = true
                }
              }
            }
            
            !done
          }
      }
  }

}
