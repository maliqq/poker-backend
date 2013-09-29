package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

class Blinds(val context: Gameplay.Context, val betting: Betting.Context) extends Stage {
  def postSB(pos: Int) {
    val bet = Bet.force(Bet.SmallBlind, context.stake)
    
    try {
      betting.force(bet)
    } catch {
    case e: Exception =>
    }
    
    val message = new Message.AddBet(pos = Some(pos), _type = Bet.SmallBlind, bet = bet)
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
    val message = new Message.AddBet(pos = Some(pos), _type = Bet.BigBlind, bet = bet)
    context.broadcast.all(message)
  }
  
  def run {
    context.moveButton
    
    val ring = context.table.ring
    
    val active = ring.active
    val waiting = ring.waiting
    
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
