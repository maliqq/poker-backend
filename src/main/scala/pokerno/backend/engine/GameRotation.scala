package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

trait GameRotation {
  g: Gameplay ⇒

  def rotateGame = if (variation isMixed)
    rotateNext { g ⇒
      game = g
      broadcast all (Message.ChangeGame(game = game))
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
