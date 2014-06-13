package de.pokerno.replay

import org.slf4j.LoggerFactory
import de.pokerno.gameplay
import de.pokerno.gameplay.{Betting => BettingTransition}
import de.pokerno.gameplay.stg
import de.pokerno.protocol.cmd
import de.pokerno.model.{Bet, BetType, Seat, Player, seat}
import concurrent.duration.Duration

private[replay] case class Betting(
    ctx: Context,
    actions: Seq[cmd.Command]
) extends gameplay.Betting with gameplay.betting.NextTurn {
  
  import ctx._
  import ctx.gameplay._
  
  private val log = LoggerFactory.getLogger(getClass)
  val gameplay = ctx.gameplay

  private val betActions = actions.filter(_.isInstanceOf[cmd.AddBet]).asInstanceOf[List[cmd.AddBet]]

  def apply() = {
    def active = round.seats.filter(_.isActive)

    val (forcedBets, activeBets) = betActions.span(_.bet.isForced)

    // пассивные ставки игроков - анте
    if (!bettingStarted && (gameOptions.hasAnte || stake.ante.isDefined)) {
      if (activeBets.isEmpty) {
        forcedBets.filter(_.bet.isInstanceOf[Bet.Ante]).foreach { ante ⇒
          val player: Player = if (ante.player != null)
            ante.player
          else {
            val seat = round.acting.get
            seat.player
          }

          table.playerSeat(player) map { seat =>
            if (seat.isActive)
              forceBet(seat, BetType.Ante)
          }
        }
      } else forceAntes()
      complete()
      sleep()
    }

    // пассивные ставки игроков - блайнды
    if (!bettingStarted && gameOptions.hasBlinds && active.size >= 2) {
      var sb: Option[seat.Sitting] = None
      var bb: Option[seat.Sitting] = None

      forcedBets.find(_.bet.isInstanceOf[Bet.SmallBlind]) foreach { bet ⇒
        sb = active.find { seat =>
          bet.player == seat.player
        }
      }

      if (sb.isDefined) {
        // FIXME
        //gameplay.setButton(sbPos - 1) // put button before SB
        forcedBets.find(_.bet.isInstanceOf[Bet.BigBlind]) foreach { bet ⇒
          bb = active.find { seat ⇒
            val found = bet.player == seat.player

            if (!found && sb.get.pos != seat.pos) {
              log.warn("{}: missing big blind", seat)
              seat.idle() // помечаем все места от SB до BB как неактивные
            }

            found
          }
        }
        
      } else {
        // FIXME
        //gameplay.moveButton

        // default blind positions
        val Seq(_sb, _bb, _*) = active // ???
        sb = Some(_sb)
        bb = Some(_bb)
      }

      Console printf("sb: %s bb: %s\n", sb, bb)

      sb.map { seat =>
        forceBet(seat, BetType.SmallBlind)
      }
      sleep()

      bb.map { seat =>
        forceBet(seat, BetType.BigBlind)
      }
      sleep()
    }

    // активные ставки игроков
    if (!activeBets.isEmpty) {
      nextTurn() match {
        case BettingTransition.Require(seat) =>
          requireBet(seat)
        
        case _ =>
      }

      activeBets.dropWhile { case cmd.AddBet(player, bet) ⇒
        val seat = round.acting.get

        log.info(" | acting #{} {}", seat.pos, seat)

        if (seat.player == player) {
          log.info(" |-- player %s bet %s" format(seat.player, bet))

          addBet(seat, bet)

          sleep()

          nextTurn() match { case BettingTransition.Require(seat) ⇒
            // continue if we have someone to act
            requireBet(seat)
            true
          case _ ⇒
            false
          }
          
        } else {
          log.warn(" |-- not our turn, dropping: %s $s" format(player, bet))
          true
        }
      }

      complete()
    }

    bettingStarted = true
  }
  
  private def forceAntes(): Unit = round.seats.filter(_.isActive).foreach { seat =>
    forceBet(seat, BetType.Ante)
  }
  
}
