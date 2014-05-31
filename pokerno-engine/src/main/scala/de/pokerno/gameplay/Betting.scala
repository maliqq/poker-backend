package de.pokerno.gameplay

import akka.actor.{ Actor, ActorRef, Cancellable }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import concurrent.duration._
import de.pokerno.gameplay.betting.NextTurn

case class Betting(ctx: StageContext, betting: ActorRef) extends Bets with NextTurn {
  
  import ctx.gameplay._
  
  var timer: Cancellable = null
  
  // turn on big bets
  def bigBets() {
    round.bigBets = true
  }
  
  // add bet
  def bet(player: Player, bet: Bet) {
    val pos = round.current
    val seat = table.seats(pos)
    if (seat.player == player) {
      if (timer != null) timer.cancel()
      Console printf("[betting] add {}", bet)
      addBet(bet)
      ctx.ref ! nextTurn()
    } else
      Console printf("[betting] not a turn of {}; current acting is {}", player, seat.player)
  }
  
  // timeout bet
  def timeout() {
    val pos = round.current
    val seat = table.seats(pos)
    
    val bet: Bet = seat.state match {
      case Seat.State.Away ⇒
        // force fold
        Bet.fold(timeout = true)

      case _ ⇒
        // force check/fold
        if (round.call == 0 || seat.didCall(round.call))
          Bet.check(timeout = true)
        else Bet.fold(timeout = true)
    }

    Console printf("[betting] timeout")
    addBet(bet)
  }
  
}

private[gameplay] object Betting {

  // start new round
  case object Start
  // require bet
  ////case class Require(amount: Decimal, limit: Game.Limit)
  // force bet
  case class Force(amount: Decimal)
  
  case class Add(player: Player, bet: Bet)

  trait Transition
  case object Next extends Transition
  // stop current deal
  case object Stop extends Transition
  // go to showdown
  case object Showdown extends Transition
  // betting done - wait for next street to occur
  case object Done extends Transition
  // require bet from this potision
  case class Require(pos: Int) extends Transition
  // start timer
  case class StartTimer(duration: FiniteDuration) extends Transition
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

//
//  trait ReplayContext extends NextTurn {
//
//    replay: Replay ⇒
//    import concurrent.duration.Duration
//    import de.pokerno.util.ConsoleUtils._
//
//    def firstStreet: Boolean
//
//    def betting(betActions: List[Betting.Add], speed: Duration) {
//      def sleep() = Thread.sleep(speed.toMillis)
//
//      val round = gameplay.round
//      debug("button=%s", round.current)
//
//      def active = round.seats.filter(_._1.isActive)
//
//      val gameOptions = gameplay.game.options
//      val stake = gameplay.stake
//      val table = gameplay.table
//
//      val (forcedBets, activeBets) = betActions.span(_.bet.betType.isInstanceOf[Bet.ForcedBet])
//
//      val anteBets = forcedBets.filter(_.bet.betType == Bet.Ante)
//
//      val postAnte = firstStreet && (gameOptions.hasAnte || stake.ante.isDefined)
//
//      // пассивные ставки игроков - анте
//      if (postAnte) {
//        if (activeBets.isEmpty) {
//          anteBets.foreach { anteBet ⇒
//            val player: Player = if (anteBet.player != null)
//              anteBet.player
//            else {
//              round.acting.get._1.player.get // FIXME
//            }
//
//            table.playerSeatWithPos(anteBet.player) map {
//              case (seat, pos) ⇒
//                if (seat.isActive) gameplay.forceBet(stageContext, (seat, pos), Bet.Ante)
//            }
//          }
//        } else {
//          val postingAnte = round.seats.filter(_._1.isActive)
//          postingAnte.foreach {
//            case (seat, pos) ⇒
//              gameplay.forceBet(stageContext, (seat, pos), Bet.Ante)
//          }
//        }
//        gameplay.completeBetting(stageContext)
//        sleep()
//      }
//
//      // пассивные ставки игроков - блайнды
//      val postBlinds = firstStreet && gameOptions.hasBlinds
//
//      val activeOnBlinds = active
//      //info("postBlinds=%s firstStreet=%s activeOnBlinds=%s", postBlinds, firstStreet, activeOnBlinds)
//      if (postBlinds && activeOnBlinds.size >= 2) {
//        var sb: Option[Tuple2[Seat, Int]] = None
//        var bb: Option[Tuple2[Seat, Int]] = None
//
//        val sbBetOption = forcedBets.find(_.bet.betType == Bet.SmallBlind)
//
//        sbBetOption foreach { sbBet ⇒
//          activeOnBlinds.find {
//            case (seat, pos) ⇒
//              seat.player.isDefined && sbBet.player == seat.player.get
//          } foreach { _sb ⇒
//            sb = Some(_sb)
//          }
//        }
//
//        if (sb.isDefined) {
//          val (sbSeat, sbPos) = sb.get
//          // FIXME
//          //gameplay.setButton(sbPos - 1) // put button before SB
//
//          val bbBetOption = forcedBets.find(_.bet.betType == Bet.BigBlind)
//
//          bbBetOption foreach { bbBet ⇒
//            activeOnBlinds.find {
//              case (seat, pos) ⇒
//                val found = seat.player.isDefined && bbBet.player == seat.player.get
//
//                if (!found && seat.player.get != sbSeat.player.get) {
//                  warn("%s: missing big blind", seat)
//                  seat.idle() // помечаем все места от SB до BB как неактивные
//                }
//
//                found
//            } map { _bb ⇒
//              bb = Some(_bb)
//            }
//          }
//
//        } else {
//          // FIXME
//          //gameplay.moveButton
//
//          // default blind positions
//          val Seq(_sb, _bb, _*) = active // ???
//          sb = Some(_sb)
//          bb = Some(_bb)
//        }
//
//        sb.map { sb ⇒ gameplay.forceBet(stageContext, sb, Bet.SmallBlind) }
//        sleep()
//
//        bb.map { bb ⇒ gameplay.forceBet(stageContext, bb, Bet.BigBlind) }
//        sleep()
//
//        debug("sb=%s bb=%s", sb, bb)
//
//        //gameplay.round.reset
//        //nextTurn()//.foreach { x => self ! x }
//      }
//
//      // активные ставки игроков
//      if (!activeBets.isEmpty) {
//        //          if (!postBlinds) {
//        //            gameplay.round.reset()
//        //          }
//        nextTurn()
//
//        //debug("activeBets=%s", activeBets)
//
//        val betsLeft = activeBets.dropWhile { addBet ⇒
//          val acting = round.acting
//          debug(" | acting %s", acting)
//          val player = acting.get._1.player
//
//          def isOurTurn = player.isDefined && player.get == addBet.player
//
//          if (isOurTurn) {
//            debug(" |-- player %s bet %s", player.get, addBet.bet)
//
//            gameplay.addBet(stageContext, addBet.bet)
//
//            sleep()
//
//            nextTurn() match {
//              case Betting.Done | Betting.Stop ⇒ false
//              case _                           ⇒ true
//            }
//          } else {
//            warn("not our turn, dropping: %s %s", addBet, acting)
//            true
//          }
//        }
//
//        gameplay.completeBetting(stageContext)
//      }
//    }
//  }

}
