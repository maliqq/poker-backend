package de.pokerno.replay

//import de.pokerno.gameplay._
//
//trait Betting extends betting.NextTurn { replay: Replay ⇒
//  import concurrent.duration.Duration
//
//  def firstStreet: Boolean
//
//  def betting(betActions: List[Betting.Add], speed: Duration) {
//    def sleep() = Thread.sleep(speed.toMillis)
//
//    val round = gameplay.round
//
//    def active = round.seats.filter(_._1.isActive)
//
//    val gameOptions = gameplay.game.options
//    val stake = gameplay.stake
//    val table = gameplay.table
//
//    val (forcedBets, activeBets) = betActions.span(_.bet.betType.isInstanceOf[Bet.ForcedBet])
//
//    val anteBets = forcedBets.filter(_.bet.betType == Bet.Ante)
//
//    val postAnte = firstStreet && (gameOptions.hasAnte || stake.ante.isDefined)
//
//    // пассивные ставки игроков - анте
//    if (postAnte) {
//      if (activeBets.isEmpty) {
//        anteBets.foreach { anteBet ⇒
//          val player: Player = if (anteBet.player != null)
//            anteBet.player
//          else {
//            round.acting.get._1.player.get // FIXME
//          }
//
//          table.playerSeatWithPos(anteBet.player) map {
//            case (seat, pos) ⇒
//              if (seat.isActive) gameplay.forceBet(stageContext, (seat, pos), Bet.Ante)
//          }
//        }
//      } else {
//        val postingAnte = round.seats.filter(_._1.isActive)
//        postingAnte.foreach {
//          case (seat, pos) ⇒
//            gameplay.forceBet(stageContext, (seat, pos), Bet.Ante)
//        }
//      }
//      gameplay.completeBetting(stageContext)
//      sleep()
//    }
//
//    // пассивные ставки игроков - блайнды
//    val postBlinds = firstStreet && gameOptions.hasBlinds
//
//    val activeOnBlinds = active
//    //info("postBlinds=%s firstStreet=%s activeOnBlinds=%s", postBlinds, firstStreet, activeOnBlinds)
//    if (postBlinds && activeOnBlinds.size >= 2) {
//      var sb: Option[Tuple2[Seat, Int]] = None
//      var bb: Option[Tuple2[Seat, Int]] = None
//
//      val sbBetOption = forcedBets.find(_.bet.betType == Bet.SmallBlind)
//
//      sbBetOption foreach { sbBet ⇒
//        activeOnBlinds.find {
//          case (seat, pos) ⇒
//            seat.player.isDefined && sbBet.player == seat.player.get
//        } foreach { _sb ⇒
//          sb = Some(_sb)
//        }
//      }
//
//      if (sb.isDefined) {
//        val (sbSeat, sbPos) = sb.get
//        // FIXME
//        //gameplay.setButton(sbPos - 1) // put button before SB
//
//        val bbBetOption = forcedBets.find(_.bet.betType == Bet.BigBlind)
//
//        bbBetOption foreach { bbBet ⇒
//          activeOnBlinds.find {
//            case (seat, pos) ⇒
//              val found = seat.player.isDefined && bbBet.player == seat.player.get
//
//              if (!found && seat.player.get != sbSeat.player.get) {
//                warn("%s: missing big blind", seat)
//                seat.idle() // помечаем все места от SB до BB как неактивные
//              }
//
//              found
//          } map { _bb ⇒
//            bb = Some(_bb)
//          }
//        }
//
//      } else {
//        // FIXME
//        //gameplay.moveButton
//
//        // default blind positions
//        val Seq(_sb, _bb, _*) = active // ???
//        sb = Some(_sb)
//        bb = Some(_bb)
//      }
//
//      sb.map { sb ⇒ gameplay.forceBet(stageContext, sb, Bet.SmallBlind) }
//      sleep()
//
//      bb.map { bb ⇒ gameplay.forceBet(stageContext, bb, Bet.BigBlind) }
//      sleep()
//
//      debug("sb=%s bb=%s", sb, bb)
//
//      //gameplay.round.reset
//      //nextTurn()//.foreach { x => self ! x }
//    }
//
//    // активные ставки игроков
//    if (!activeBets.isEmpty) {
//      //          if (!postBlinds) {
//      //            gameplay.round.reset()
//      //          }
//      nextTurn()
//
//      //debug("activeBets=%s", activeBets)
//
//      val betsLeft = activeBets.dropWhile { addBet ⇒
//        val acting = round.acting
//        debug(" | acting %s", acting)
//        val player = acting.get._1.player
//
//        def isOurTurn = player.isDefined && player.get == addBet.player
//
//        if (isOurTurn) {
//          debug(" |-- player %s bet %s", player.get, addBet.bet)
//
//          gameplay.addBet(stageContext, addBet.bet)
//
//          sleep()
//
//          nextTurn() match {
//            case Betting.Done | Betting.Stop ⇒ false
//            case _                           ⇒ true
//          }
//        } else {
//          warn("not our turn, dropping: %s %s", addBet, acting)
//          true
//        }
//      }
//
//      gameplay.completeBetting(stageContext)
//    }
//  }
//}