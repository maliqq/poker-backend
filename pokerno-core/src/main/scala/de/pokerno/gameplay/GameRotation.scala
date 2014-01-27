package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.{msg => message}

trait GameRotation {

  g: GameplayLike ⇒

  def rotateGame(ctx: StageContext) = if (variation isMixed)
    rotateNext { g ⇒
      game = g
      events.gameChange(game)
    }

  final val rotateEvery = 8

  private var _rotationIndex = 0
  private var _rotationCounter = 0

  private def nextGame = {
    val mix = variation.asInstanceOf[Mix]
    _rotationIndex += 1
    _rotationIndex %= mix.games.size
    mix.games(_rotationIndex)
  }

  private def rotateNext(f: Game ⇒ Unit) {
    _rotationCounter += 1
    if (_rotationCounter > rotateEvery) {
      _rotationCounter = 0
      f(nextGame)
    }
  }
  
}
