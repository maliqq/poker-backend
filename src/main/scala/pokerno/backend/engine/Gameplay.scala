package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

object Gameplay {
  trait Rotation {
    def variation: Variation
    
    final val rotateEvery = 8
    
    private var _rotationIndex = 0
    private var _rotationCounter = 0
    
    protected def rotateNext(f: Game => Unit) {
      _rotationCounter += 1
      if (_rotationCounter > rotateEvery) {
        _rotationCounter = 0
        f(nextGame)
      }
    }
    
    private def nextGame = {
      val mix = variation.asInstanceOf[Mix]
      _rotationIndex += 1
      _rotationIndex %= mix.games.size
      mix.games(_rotationIndex)
    }
  }
  
  trait Antes {
  c: Context =>
    
    def postAntes {
      val active = table.where(_.isActive)
      
      betting = new Betting.Context(active)
      (0 to betting.size) foreach { _ =>
        forceBet(Bet.Ante)
        betting.move
      }
    }
  }
  
  trait Blinds {
  c: Context =>
    def postBlinds {
      moveButton
      
      val active = table.where(_.isActive)
      val waiting = table.where(_.isWaitingBB)
      
      if (active.size + waiting.size >= 2) {
        betting = new Betting.Context(active)
        
        forceBet(Bet.SmallBlind)
        betting.move
        
        forceBet(Bet.BigBlind)
        betting.move
      }
    }
  }
  
  class Context(
    val dealer: Dealer,
    val broadcast: Broadcast,
    val variation: Variation,
    var betting: Betting.Context,
    val stake: Stake,
    val table: Table
  ) extends Rotation with Antes with Blinds with Showdown {
    
    var game: Game = variation match {
      case g: Game => g
      case m: Mix => m.games.head
    }
    
    def moveButton {
      table.moveButton
      broadcast.all(Message.MoveButton(pos = table.button))
    }
    
    def setButton(pos: Int) {
      table.button = pos
      broadcast.all(Message.MoveButton(pos = table.button))
    }
    
    def forceBet(betType: Bet.Value) {
      val bet = Bet.force(betType, stake)
    
      try {
        betting.force(bet)
        broadcast.all(Message.AddBet(Bet.Ante, pos = Some(betting.pos), bet = bet))
      } catch {
      case e: Exception =>
      }
    }
    
    def run {
      table.where(_.isReady).map(_._1.play)
      
      rotateNext { g =>
        game = g
        broadcast.all(Message.ChangeGame(game = game))
      }
      
      val streets = Streets.ByGameGroup(game.options.group)
      for (street <- streets) {
        street.run(this)
      }
    }
  }
}
