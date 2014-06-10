package de.pokerno.replay

import org.slf4j.LoggerFactory
import de.pokerno.gameplay
import de.pokerno.gameplay.stg
import de.pokerno.protocol.cmd
import de.pokerno.model.{Bet, Seat, Player}
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
            val pos = round.current
            val seat = table.seats(pos)
            seat.player.get
          }

          table.playerSeat(player) map { seat =>
            if (seat.isActive)
              forceBet(seat.pos, Bet.Ante)
          }
        }
      } else forceAntes()
      doneBets()
      sleep()
    }

    // пассивные ставки игроков - блайнды
    if (!bettingStarted && gameOptions.hasBlinds && active.size >= 2) {
      var sb: Option[Seat] = None
      var bb: Option[Seat] = None

      forcedBets.find(_.bet.isInstanceOf[Bet.SmallBlind]) foreach { bet ⇒
        sb = active.find { seat =>
          seat.player.isDefined && bet.player == seat.player.get
        }
      }

      if (sb.isDefined) {
        // FIXME
        //gameplay.setButton(sbPos - 1) // put button before SB
        forcedBets.find(_.bet.isInstanceOf[Bet.BigBlind]) foreach { bet ⇒
          bb = active.find { seat ⇒
            val found = seat.player.isDefined && bet.player == seat.player.get

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
        forceBet(seat.pos, Bet.SmallBlind)
      }
      sleep()

      bb.map { seat =>
        forceBet(seat.pos, Bet.BigBlind)
      }
      sleep()
    }

    // активные ставки игроков
    if (!activeBets.isEmpty) {
      nextTurn() match {
        case Left(pos) => requireBet(pos)
        case _ =>
      }

      activeBets.dropWhile { case cmd.AddBet(player, bet) ⇒
        val pos = round.current
        val seat = table.seats(pos)

        log.info(" | acting #{} {}", pos, seat)

        def isOurTurn = seat.player.isDefined && seat.player.get == player

        if (isOurTurn) {
          log.info(" |-- player %s bet %s" format(seat.player.get, bet))

          addBet(bet)

          sleep()

          nextTurn() match {
            case Left(pos) ⇒
              requireBet(pos)
              true // continue if we have someone to act
            case _       ⇒ false
          }
        } else {
          log.warn(" |-- not our turn, dropping: %s $s" format(player, bet))
          true
        }
      }

      doneBets()
    }

    bettingStarted = true
  }
  
  private def forceAntes(): Unit = round.seats.filter(_.isActive).foreach { seat =>
    forceBet(seat.pos, Bet.Ante)
  }
  
}
