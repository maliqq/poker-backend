package de.pokerno.gameplay

import akka.actor.{Actor, ActorRef, Cancellable}
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.protocol.{ msg ⇒ message }
import de.pokerno.protocol.{ rpc, wire, cmd }
import wire.Conversions._
import de.pokerno.protocol.Conversions._
import concurrent.duration._

private[gameplay] trait Betting
    extends Antes
    with Blinds {

  import de.pokerno.util.ConsoleUtils._

  // require bet
  def requireBet(ctx: StageContext, acting: Tuple2[Seat, Int]) {
    val round = ctx.gameplay.round
    round requireBet acting
    info("[betting] require %s to call %s and raise %s", acting, round.call, round.raise)
    ctx.gameplay.events.requireBet(round.box, round.call, round.raise)
  }

  // add bet
  def addBet(ctx: StageContext, bet: Bet) {
    val round = ctx.gameplay.round
    val posted = round.addBet(bet)
    ctx.gameplay.events.addBet(round.box, posted)
  }

  // force bet
  def forceBet(ctx: StageContext, acting: Tuple2[Seat, Int], _type: Bet.ForcedBet) {
    val round = ctx.gameplay.round
    val posted = round.forceBet(acting, _type)
    ctx.gameplay.events.addBet(round.box, posted)
  }

  // current betting round finished
  def completeBetting(ctx: StageContext) {
    val table = ctx.gameplay.table
    table.seatsAsList.filter(_ inPlay) map (_ play)

    val round = ctx.gameplay.round
    round.clear()

    ctx.gameplay.events.declarePot(
      round.pot.total,
      round.pot.sidePots.map(_.total))
  }

}

private[gameplay] object Betting {

  // start new round
  case object Start
  // require bet
  case class Require(amount: Decimal, limit: Game.Limit)
  // force bet
  case class Force(amount: Decimal)

  trait Transition
  case object Next extends Transition
  // stop current deal
  case object Stop extends Transition
  // betting done - wait for next street to occur
  case object Done extends Transition
  // start timer
  case class StartTimer(duration: FiniteDuration) extends Transition
  // betting timeout - go to next seat
  case object Timeout
  // turn on big bet mode
  case object BigBets

  trait NextTurn {
    import de.pokerno.util.ConsoleUtils._

    def gameplay: Context
    def stageContext: StageContext

    protected def nextTurn(): Transition = {
      val round = gameplay.round

      //Console printf("%s%s%s\n", Console.MAGENTA, gameplay.table, Console.RESET)

      round.seats filter (_._1 inPlay) foreach {
        case (seat, pos) ⇒
          if (!seat.didCall(round.call)) {
            //warn("not called, still playing: %s", seat)
            seat.playing()
          }
      }

      if (round.seats.filter(_._1 inPot).size < 2) {
        info("[betting] should stop")
        return Betting.Stop
      }

      val playing = round.seats filter (_._1 isPlaying)
      if (playing.size == 0) {
        info("[betting] should done")
        return Betting.Done
      }

      gameplay.requireBet(stageContext, playing.head)

      Betting.StartTimer(30 seconds)
    }

  }

  trait DealContext extends NextTurn {
    deal: Deal ⇒
    
    import context._
    
    var timer: Cancellable = null

    def handleBetting: Receive = {
      case cmd.AddBet(player, bet) ⇒
        val (seat, pos) = gameplay.round.acting
        if (seat.player.map(_.id == player).getOrElse(false)) {
          if (timer != null)
            timer.cancel()
          log.info("[betting] add {}", bet)
          gameplay.addBet(stageContext, bet)
          self ! nextTurn()
        } else
          log.warning("[betting] not a turn of {}; current acting is {}", player, seat.player)

      case Betting.Stop ⇒
        log.info("[betting] stop")
        context.become(handleStreets)
        self ! Streets.Done
      
      case Betting.StartTimer(duration) =>
        timer = system.scheduler.scheduleOnce(duration, self, Betting.Timeout)

      case Betting.Timeout ⇒
        val round = gameplay.round
        val (seat, pos) = round.acting
        
        val bet: Bet = seat.state match {
          case Seat.State.Away =>
            // force fold
            Bet.fold(timeout = true)
          
          case _ =>
            // force check/fold
            if (round.call == 0 || seat.didCall(round.call))
              Bet.check(timeout = true)
            else Bet.fold(timeout = true)
        }
        
        log.info("[betting] timeout")
        gameplay.addBet(stageContext, bet)
        self ! nextTurn()

      case Betting.Done ⇒
        log.info("[betting] done")
        gameplay.completeBetting(stageContext)
        context.become(handleStreets)
        streets(stageContext)

      case Betting.BigBets ⇒
        log.info("[betting] big bets")
        gameplay.round.bigBets = true
    }

  }

  trait ReplayContext extends NextTurn {

    replay: Replay ⇒
    import concurrent.duration.Duration
    import de.pokerno.util.ConsoleUtils._

    def firstStreet: Boolean

    def betting(betActions: List[cmd.AddBet], speed: Duration) {
      def sleep() = Thread.sleep(speed.toMillis)

      val round = gameplay.round
      debug("button=%s", round.current)

      def active = round.seats.filter(_._1.isActive)

      val gameOptions = gameplay.game.options
      val stake = gameplay.stake
      val table = gameplay.table

      val (forcedBets, activeBets) = betActions.span { addBet ⇒
        (addBet.bet.getType: Bet.Value).isInstanceOf[Bet.ForcedBet]
      }

      val anteBets = forcedBets.filter { addBet ⇒
        (addBet.bet.getType: Bet.Value) == Bet.Ante
      }

      val postAnte = firstStreet && (gameOptions.hasAnte || stake.ante.isDefined)

      // пассивные ставки игроков - анте
      if (postAnte) {
        if (activeBets.isEmpty) {
          anteBets.foreach { anteBet ⇒
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
              if (seat.isActive) gameplay.forceBet(stageContext, (seat, pos), Bet.Ante)
            }
          }
        } else {
          val postingAnte = round.seats.filter(_._1.isActive)
          postingAnte.foreach {
            case (seat, pos) ⇒
              gameplay.forceBet(stageContext, (seat, pos), Bet.Ante)
          }
        }
        gameplay.completeBetting(stageContext)
        sleep()
      }

      // пассивные ставки игроков - блайнды
      val postBlinds = firstStreet && gameOptions.hasBlinds

      val activeOnBlinds = active
      //info("postBlinds=%s firstStreet=%s activeOnBlinds=%s", postBlinds, firstStreet, activeOnBlinds)
      if (postBlinds && activeOnBlinds.size >= 2) {
        var sb: Option[Tuple2[Seat, Int]] = None
        var bb: Option[Tuple2[Seat, Int]] = None

        val sbBetOption = forcedBets.find { addBet ⇒
          (addBet.bet.getType: Bet.Value) == Bet.SmallBlind
        }

        sbBetOption foreach { sbBet ⇒
          activeOnBlinds.find {
            case (seat, pos) ⇒
              seat.player.isDefined && sbBet.player == seat.player.get.id
          } foreach { _sb ⇒
            sb = Some(_sb)
          }
        }

        if (sb.isDefined) {
          val (sbSeat, sbPos) = sb.get
          // FIXME
          //gameplay.setButton(sbPos - 1) // put button before SB

          val bbBetOption = forcedBets.find { addBet ⇒
            (addBet.bet.getType: Bet.Value) == Bet.BigBlind
          }

          bbBetOption foreach { bbBet ⇒
            activeOnBlinds.find {
              case (seat, pos) ⇒
                val found = seat.player.isDefined && bbBet.player == seat.player.get.id

                if (!found && seat.player.get != sbSeat.player.get) {
                  warn("%s: missing big blind", seat)
                  seat.idle() // помечаем все места от SB до BB как неактивные
                }

                found
            } map { _bb ⇒
              bb = Some(_bb)
            }
          }

        } else {
          // FIXME
          //gameplay.moveButton

          // default blind positions
          val List(_sb, _bb, _*) = active
          sb = Some(_sb)
          bb = Some(_bb)
        }

        sb.map { sb ⇒ gameplay.forceBet(stageContext, sb, Bet.SmallBlind) }
        sleep()

        bb.map { bb ⇒ gameplay.forceBet(stageContext, bb, Bet.BigBlind) }
        sleep()

        debug("sb=%s bb=%s", sb, bb)

        //gameplay.round.reset
        //nextTurn()//.foreach { x => self ! x }
      }

      // активные ставки игроков
      if (!activeBets.isEmpty) {
        //          if (!postBlinds) {
        //            gameplay.round.reset()
        //          }
        nextTurn()

        //debug("activeBets=%s", activeBets)

        val betsLeft = activeBets.dropWhile { addBet ⇒
          val acting = round.acting
          debug(" | acting %s", acting)
          val player = acting._1.player

          def isOurTurn = player.isDefined && player.get.id == addBet.player

          if (isOurTurn) {
            debug(" |-- player %s bet %s", player.get, addBet.bet)
            
            gameplay.addBet(stageContext, addBet.bet)
            
            sleep()
            
            nextTurn() match {
              case Betting.Done | Betting.Stop => false
              case _ => true
            }
          } else {
            warn("not our turn, dropping: %s %s", addBet, acting)
            true
          }
        }

        gameplay.completeBetting(stageContext)
      }
    }
  }

}
