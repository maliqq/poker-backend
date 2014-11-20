package de.pokerno.form

import akka.actor.ActorRef

import de.pokerno.protocol.{cmd, api}
import de.pokerno.gameplay
import room.Watchers

abstract class CashRoom extends Room with cash.JoinLeave with cash.Cycle with cash.Presence {
  import context._
  import Room._
  
  val balance: de.pokerno.payment.thrift.Payment.FutureIface
  val history: Option[ActorRef]
  val persist: Option[ActorRef]
  val watchers: ActorRef
  
  startWith(State.Waiting, NoneRunning)
  
  paused {
    case Event(Resume, _) ⇒ toActive()
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
      context.stop(deal)
      toClosed()

    case Event(Pause, Running(_, deal)) ⇒
      context.stop(deal)
      toPaused()

    // first deal in active state
    case Event(gameplay.Deal.Start, NoneRunning) ⇒
      tryDealStart()

    // current deal cancelled
    case Event(gameplay.Deal.Cancel, Running(_, deal)) ⇒
      log.info("deal cancelled")
      toWaiting() using (NoneRunning)

    // current deal stopped
    case Event(gameplay.Deal.Done(after), Running(ctx, deal)) ⇒
      log.info("deal complete")
      
      history.map { _ ! (id, ctx.game, ctx.stake, ctx.play) }
      
      self ! gameplay.Deal.Next(after + nextDealAfter) // FIXME
      
      stay() using (NoneRunning)

    // schedule next deal in *after* seconds
    case Event(gameplay.Deal.Next(after), NoneRunning) ⇒
      log.info("next deal will start in {}", after)
      events.broadcast(gameplay.Events.announceStart(after))
      system.scheduler.scheduleOnce(after, self, gameplay.Deal.Start)
      stay()

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
      stay()
  }

  whenUnhandled {
    case Event(Room.Observe(observer, name), _) ⇒
      events.broker.subscribe(observer, name)
      // TODO !!!!!
      //events.start(table, variation, stake, )
      stay()
    
    case Event(Room.Subscribe(name), _) =>
      events.broker.subscribe(sender, name)
      stay()

    case Event(Connect(conn), current) ⇒
      // notify seat state change
      conn.player map (playerOnline(_))

      // send start message
      val startMsg: de.pokerno.protocol.GameEvent = current match {
        case NoneRunning ⇒
          gameplay.Events.start(roomId, table, variation, stake, conn.player) // TODO: empty play
        case Running(ctx, deal) ⇒
          gameplay.Events.start(ctx, conn.player)
      }
    
      conn.send(de.pokerno.protocol.GameEvent.encode(startMsg))

      watchers ! Watchers.Watch(conn)

      // start new deal if needed
      tryResume()

    case Event(Disconnect(conn), _) ⇒
      watchers ! Watchers.Unwatch(conn)
      //events.broker.unsubscribe(observer, conn.player.getOrElse(conn.sessionId))
      conn.player map (playerOffline(_))
      stay()

    case Event(Away(player), _) ⇒
      playerAway(player)
      stay()

    case Event(kick: cmd.KickPlayer, _) ⇒
      leavePlayer(kick.player)
      stay()
   
    case Event(cmd.ComeBack(player), _) =>
      table(player).map { seat =>
        if (seat.isSitOut) {
          seat.ready()
          events broadcast gameplay.Events.playerComeBack(seat)
        }
      }
      tryResume()
      
    case Event(cmd.SitOut(player), current) =>
      table(player).map { seat =>
        current match {
          case NoneRunning =>
            // do sit out immediately
            seat.idle()
            events broadcast gameplay.Events.playerSitOut(seat)
            
          case _ =>             seat.toggleSittingOut()
        }
      }
      
      stay()
    
    case Event(cmd.AdvanceStack(player, amount), _) =>
      table(player).map { seat =>
        if (seat.isTaken) {
          buyInSeat(seat, amount)
        } else {
          seat.buyIn(amount)
          // TODO ???
        }
      }
      tryResume()
      
    case Event(PlayState, NoneRunning) =>
      sender ! api.PlayState(roomId, table, variation, stake)
      stay()
      
    case Event(PlayState, Running(ctx, _)) =>
      sender ! api.PlayState(ctx)
      stay()
      
    case Event(x: Any, _) ⇒
      log.warning("unhandled: {}", x)
      stay()
  }
  
  onTransition {
    case State.Waiting -> State.Active ⇒
      self ! gameplay.Deal.Next(firstDealAfter)
      persist.map { _ ! Room.ChangedState(roomId, State.Active) }
    case State.Active -> State.Waiting =>
      persist.map { _ ! Room.ChangedState(roomId, State.Waiting) }
  }
  
}
