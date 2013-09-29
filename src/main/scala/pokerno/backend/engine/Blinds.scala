package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

object Blinds extends Stage {
  def run(context: Gameplay.Context) = {
    
  }
}

class Blinds(val context: Gameplay.Context, val betting: Betting.Context) {
  def postSB(pos: Int) {
    val bet = Bet.force(Bet.SmallBlind, context.stake)
    
    try {
      betting.force(bet)
    } catch {
    case e: Exception =>
    }
    
    val message = Message.AddBet(pos = Some(pos), _type = Bet.SmallBlind, bet = bet)
    context.broadcast.all(message)
  }
  
  def postBB(pos: Int) {
    val (seat, pos) = betting.current
    
    val bet = Bet.force(Bet.BigBlind, context.stake)
    try {
      betting.force(bet)
    } catch {
    case e: Exception =>
    }
    val message = Message.AddBet(pos = Some(pos), _type = Bet.BigBlind, bet = bet)
    context.broadcast.all(message)
  }
  
  def run {
    context.moveButton
    
    val active = context.table.active
    val waiting = context.table.waiting
    
    if (active.size + waiting.size >= 2) {
      betting.start(active)
      
      betting.step { case (seat, pos) =>
        postSB(pos)
      }
      
      betting.step { case (seat, pos) =>
        postBB(pos)
      }
    }
  }
}
