package de.pokerno.gameplay

import akka.actor.ActorRef
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


  trait Transition
  case object Next extends Transition
  // stop current deal
  case object Stop extends Transition
  // betting done - wait for next street to occur
  case object Done extends Transition
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets
  
  trait NextTurn {
    
    def gameplay: ContextLike
    
    protected def nextTurn(): Option[Transition] = {
      val round = gameplay.round
      
      round.move()
      round.seats filter (_._1 inPlay) foreach {
        case (seat, pos) ⇒
          if (!seat.isCalled(round.call)) seat.playing()
      }
  
      if (round.seats.filter(_._1 inPot).size < 2)
        return Some(Betting.Stop)
      val active = round.seats filter (_._1 isPlaying)

      if (active.size == 0)
        return Some(Betting.Done)
      
      round requireBet active.head
      
      None
    }
    
  }

  trait DealContext extends NextTurn {
    deal: Deal =>
      
  
    def handleBetting: Receive = {
      case Betting.Add(player, bet) ⇒
        val (seat, pos) = gameplay.round.acting
        if (seat.player.isDefined && seat.player.get == player) {
          log.info("[betting] add {}", bet)
          gameplay.round.addBet(bet)
          nextTurn().foreach(self ! _)
        } else
          log.warning("[betting] not a turn of {}; current acting is {}", player, seat.player)
  
      case Betting.Stop ⇒
        log.info("[betting] stop")
        context.become(handleStreets)
        self ! Streets.Done
        
      case Betting.Timeout ⇒
        log.info("[betting] timeout")
        nextTurn().foreach(self ! _)
  
      case Betting.Done ⇒
        log.info("[betting] done")
        gameplay.round.complete()
        context.become(handleStreets)
        streets(stageContext)
  
      case Betting.BigBets ⇒
        log.info("[betting] big bets")
        gameplay.round.bigBets = true
    }
    
  }
  
  trait ReplayContext extends NextTurn {
    
    replay: Replay =>
      
      def firstStreet: Boolean
      
      def betting(betActions: List[rpc.AddBet]) {
        val round = gameplay.round
        
        def active = round.seats.filter(_._1.isActive)
        
        val gameOptions = gameplay.game.options
        val stake = gameplay.stake
        val table = gameplay.table
        
        val (forcedBets, activeBets) = betActions.span { addBet =>
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
        //log.info("postBlinds={} firstStreet={} activeBeforeButtonMove={}", postBlinds, firstStreet, activeBeforeButtonMove)
        if (postBlinds && activeBeforeButtonMove.size >= 2) {
          var sb: Option[Tuple2[Seat, Int]] = None
          var bb: Option[Tuple2[Seat, Int]] = None
          
          val sbBetOption = forcedBets.find { addBet =>
            (addBet.bet.getType: Bet.Value) == Bet.SmallBlind
          }
          
          sbBetOption foreach { sbBet =>
            activeBeforeButtonMove.find { case (seat, pos) =>
              seat.player.isDefined && sbBet.player == seat.player.get.id
            } foreach { _sb =>
              sb = Some(_sb)
            }
          }
          
          if (sb.isDefined) {
            val (sbPlayer, sbPos) = sb.get
            gameplay.setButton(sbPos - 1) // put button before SB
            
            forcedBets.find { addBet =>
              (addBet.bet.getType: Bet.Value) == Bet.BigBlind
            } foreach { bbBet =>
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
          
          log.info("sb={} bb={}", sb, bb)
          
          sb.map { sb => round.forceBet(sb, Bet.SmallBlind) }
          bb.map { bb => round.forceBet(bb, Bet.SmallBlind) }
          
          //gameplay.round.reset
          nextTurn()//.foreach { x => self ! x }
        }
        
        // активные ставки игроков
        if (!activeBets.isEmpty) {
          //gameplay.round.reset
          
          activeBets.takeWhile { addBet =>
            val acting = round.acting
            log.info("| -- acting {}", acting)
            val player = acting._1.player
            
            def isOurTurn = player.isDefined && player.get.id == addBet.player
            
            if (isOurTurn) {
              log.info("| --- player {} bet {}", player, addBet.bet)
              round.addBet(addBet.bet)
              nextTurn().forall { x => self ! x; false }
            } else true
          }
          
          // TODO complete bets
        }
      }
  }

}
