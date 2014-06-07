package de.pokerno.replay

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
  
  val gameplay = ctx.gameplay

  private val betActions = actions.filter(_.isInstanceOf[cmd.AddBet]).asInstanceOf[List[cmd.AddBet]]

  def apply() = {
    def active = round.seats.filter(_._1.isActive)

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

          table.playerPos(player) map { pos =>
            val seat = table.seats(pos)
            if (seat.isActive)
              forceBet(pos, Bet.Ante)
          }
        }
      } else forceAntes()
      doneBets()
      sleep()
    }

    // пассивные ставки игроков - блайнды
    if (!bettingStarted && gameOptions.hasBlinds && active.size >= 2) {
      var sb: Option[Int] = None
      var bb: Option[Int] = None

      forcedBets.find(_.bet.isInstanceOf[Bet.SmallBlind]) foreach { bet ⇒
        sb = active.find {
          case (seat, pos) ⇒
            seat.player.isDefined && bet.player == seat.player.get
        }.map(_._2)
      }

      if (sb.isDefined) {
        // FIXME
        //gameplay.setButton(sbPos - 1) // put button before SB
        forcedBets.find(_.bet.isInstanceOf[Bet.BigBlind]) foreach { bet ⇒
          bb = active.find { case (seat, pos) ⇒
            val found = seat.player.isDefined && bet.player == seat.player.get

            if (!found && sb.get != pos) {
              Console printf("%s: missing big blind\n", seat)
              seat.idle() // помечаем все места от SB до BB как неактивные
            }

            found
          }.map(_._2)
        }
        
      } else {
        // FIXME
        //gameplay.moveButton

        // default blind positions
        val Seq(_sb, _bb, _*) = active // ???
        sb = Some(_sb._2)
        bb = Some(_bb._2)
      }

      Console printf("sb: %s bb: %s\n", sb, bb)

      sb.map(forceBet(_, Bet.SmallBlind))
      sleep()

      bb.map(forceBet(_, Bet.BigBlind))
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

        Console printf(" | acting #%s %s\n", pos, seat)

        def isOurTurn = seat.player.isDefined && seat.player.get == player

        if (isOurTurn) {
          Console printf(" |-- player %s bet %s\n", seat.player.get, bet)

          addBet(bet)

          sleep()

          nextTurn() match {
            case Left(pos) ⇒
              requireBet(pos)
              true // continue if we have someone to act
            case _       ⇒ false
          }
        } else {
          Console printf(" |-- not our turn, dropping: %s %s\n", player, bet)
          true
        }
      }

      doneBets()
    }

    bettingStarted = true
  }
  
  private def forceAntes(): Unit = round.seats.filter(_._1.isActive).foreach {
    case (seat, pos) ⇒
      forceBet(pos, Bet.Ante)
  }
  
}
