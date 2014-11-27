package de.pokerno.gameplay.replay

import org.slf4j.LoggerFactory
import de.pokerno.gameplay
import de.pokerno.gameplay.Round
import gameplay.betting.NextTurn
import de.pokerno.protocol.cmd
import de.pokerno.model.{Bet, BetType, Player}
import de.pokerno.model.seat.impl
import de.pokerno.model.seat.impl.Sitting
import concurrent.duration.Duration

private[replay] case class Betting(
    ctx: Context,
    actions: Seq[cmd.Command]
) extends gameplay.Betting {
  
  private val log = LoggerFactory.getLogger(getClass)
  
  import ctx._
  import ctx.gameplay._
  
  val gameplay = ctx.gameplay
  
  override def round = bettingRound
  
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

          table(player) map { seat =>
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
      var sb: Option[Sitting] = None
      var bb: Option[Sitting] = None

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
        case Round.Require(seat) =>
          require(seat)
        
        case _ =>
      }

      activeBets.dropWhile { case cmd.AddBet(player, bet) ⇒
        val seat = round.acting.get

        log.info(" | acting #{} {}", seat.pos, seat)

        if (seat.player == player) {
          log.info(" |-- player %s bet %s" format(seat.player, bet))

          addBet(seat, bet)

          sleep()

          nextTurn() match { case Round.Require(seat) ⇒
            // continue if we have someone to act
            require(seat)
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
