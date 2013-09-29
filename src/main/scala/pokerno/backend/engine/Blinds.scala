package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

object PostBlinds extends Stage {
  def run(context: Gameplay.Context) = {
    
  }
}

class PostBlinds(val context: Gameplay.Context) {
  def postSB(pos: Int) {
    val bet = Bet.force(Bet.SmallBlind, context.stake)
    
    try {
      context.betting.force(bet)
    } catch {
    case e: Exception =>
    }
    
    val message = Message.AddBet(pos = Some(pos), _type = Bet.SmallBlind, bet = bet)
    context.broadcast.all(message)
  }
  
  def postBB(pos: Int) {
    val (seat, pos) = context.betting.current
    
    val bet = Bet.force(Bet.BigBlind, context.stake)
    try {
      context.betting.force(bet)
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
      context.betting.start(active)
      
      context.betting.step { case (seat, pos) =>
        postSB(pos)
      }
      
      context.betting.step { case (seat, pos) =>
        postBB(pos)
      }
    }
  }
}
