package de.pokerno.form

import akka.actor.ActorRef

import de.pokerno.protocol.{cmd, api, GameEvent}
import de.pokerno.gameplay

abstract class CashRoom extends Room with cash.JoinLeave with cash.Cycle with cash.Presence {
  import context._
  import Room._

  override def buildRoomEvents() = new RoomEvents(List(
    Topics.Deals,
    Topics.State,
    Topics.Metrics
  ))

  val balance: de.pokerno.payment.thrift.Payment.FutureIface

  startWith(State.Waiting, NoneRunning)

  paused {
    case Event(Resume, NoneRunning) ⇒
      log.info("resuming")
      toActive()

    case Event(gameplay.Deal.Done, Running(ctx, deal)) ⇒
      log.info("deal complete in paused mode")
      roomEvents.publish(gameplay.Deal.dump(id, ctx), to = Topics.Deals)
      stay() using(NoneRunning)
  }

  waiting {
    case Event(join: cmd.JoinPlayer, NoneRunning) ⇒
      joinPlayer(join)
      tryResume()
  }

  closed {
    case Event(x: Any, _) ⇒
      log.warning("got {} in closed state", x)
      stay()
  }

  active {
    case Event(Close, Running(_, deal)) ⇒
      // FIXME
      context.stop(deal)
      toClosed()

    case Event(Pause, Running(_, deal)) ⇒
      toPaused()

    // first deal in active state
    case Event(gameplay.Deal.Start, NoneRunning) ⇒
      tryDealStart()

    // current deal cancelled
    case Event(gameplay.Deal.Cancel, Running(_, deal)) ⇒
      log.info("deal cancelled")
      events.broadcast(gameplay.Events.dealCancel())
      toWaiting() using (NoneRunning)

    // current deal stopped
    case Event(gameplay.Deal.Done, Running(ctx, deal)) ⇒
      log.info("deal complete")

      roomEvents.publish(gameplay.Deal.dump(id, ctx), to = Topics.Deals)

      self ! gameplay.Deal.Next(nextDealAfter) // FIXME

      stay() using (NoneRunning)

    // schedule next deal in *after* seconds
    case Event(gameplay.Deal.Next(after), NoneRunning) ⇒
      log.info("next deal will start in {}", after)
      events.broadcast(gameplay.Events.announceStart(after))
      system.scheduler.scheduleOnce(after, self, gameplay.Deal.Start)
      stay()
  }

  whenUnhandled {
    // used in pokerno-ai:
//    case Event(Room.Observe(observer, name), _) ⇒
//      events.broker.subscribe(observer, name)
//      // TODO !!!!!
//      //events.start(table, variation, stake, )
//      stay()
    // used in pokerno-ai:
//    case Event(Room.Subscribe(name), _) =>
//      events.broker.subscribe(sender, name)
//      stay()

    // add bet
    case Event(addBet: cmd.AddBet, Running(_, deal)) ⇒
      deal ! gameplay.Betting.Add(addBet.player, addBet.bet) // pass to deal
      stay()

    // discard cards
    case Event(discard: cmd.DiscardCards, Running(_, deal)) ⇒
      deal ! gameplay.Discarding.Discard(discard.player, discard.cards) // pass to deal
      stay()

    case Event(join: cmd.JoinPlayer, _) ⇒
      joinPlayer(join)
      stay()

    case Event(chat: cmd.Chat, _) ⇒
      // TODO broadcast
      events broadcast gameplay.Events.chat(chat.player, chat.message)
      stay()

    case Event(Connect(conn), current) ⇒
      // notify seat state change
      stateName match {
      case State.Closed =>
        conn.send(gameplay.Events.error("room is closed"))
        conn.close()
        stay()

      case _ =>
        watchers.subscribe(conn)
        conn.player map (playerOnline(_))

        // send start message
        val startMsg: GameEvent = current match {
          case NoneRunning ⇒
            gameplay.Events.start(roomId, stateName.toString(), table, variation, stake, conn.player) // TODO: empty play
          case Running(ctx, deal) ⇒
            gameplay.Events.start(ctx, stateName.toString(), conn.player)
        }

        conn.send(GameEvent.encode(startMsg))

        // start new deal if needed
        tryResume()
      }

    case Event(Disconnect(conn), _) ⇒
      watchers.unsubscribe(conn)
      //events.broker.unsubscribe(observer, conn.player.getOrElse(conn.sessionId))
      conn.player map (playerOffline(_))
      stay()

    case Event(Away(player), _) ⇒
      playerAway(player)
      stay()

    case Event(kick: cmd.KickPlayer, _) ⇒
      leavePlayer(kick.player, kick = true)
      stay()

    case Event(kick: cmd.LeavePlayer, _) ⇒
      leavePlayer(kick.player)
      stay()

    case Event(cmd.ComeBack(player), _) =>
      playerComeBack(player)
      tryResume()

    case Event(cmd.SitOut(player), current) =>
      playerSitOut(player, isRunning)
      stay()

    case Event(cmd.Rebuy(player), _) =>
      table(player).map { seat =>
        askRebuy(seat)
      }
      stay()

    case Event(cmd.AdvanceStack(player, amount), _) =>
      table(player).map { seat =>
        if (seat.isTaken) {
          buyInSeat(seat, amount)
        } else {
          rebuySeat(seat, amount)
        }
      }
      tryResume()

    case Event(RoomState, NoneRunning) =>
      sender ! api.RoomState(roomId, stateName.toString(), table, variation, stake)
      stay()

    case Event(RoomState, Running(ctx, _)) =>
      sender ! api.RoomState(ctx, stateName.toString())
      stay()

    case Event(x: Any, _) ⇒
      log.warning("unhandled: {}", x)
      stay()
  }

  onTransition {
    case State.Active -> State.Paused =>
      //roomEvents.publish(Room.ChangedState(roomId, State.Paused), to = Topics.State)

    case State.Paused -> State.Active =>
      self ! gameplay.Deal.Next(firstDealAfter)

    case State.Waiting -> State.Active ⇒
      self ! gameplay.Deal.Next(firstDealAfter)
      roomEvents.publish(Room.ChangedState(roomId, State.Active), to = Topics.State)

    case State.Active -> State.Waiting =>
      roomEvents.publish(Room.ChangedState(roomId, State.Waiting), to = Topics.State)
  }

}
