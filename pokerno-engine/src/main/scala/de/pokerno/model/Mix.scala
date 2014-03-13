package de.pokerno.model

object Mix {
  final val MaxTableSize = 8
}

case class Mix(game: Game.Mixed, var _tableSize: Int = Mix.MaxTableSize) extends Variation {
  val options = Mixes(game)
  if (_tableSize > Mix.MaxTableSize)
    _tableSize = Mix.MaxTableSize
  val tableSize = _tableSize
  val games = options.map { option ⇒
    new Game(option._1, Some(option._2), Some(tableSize))
  }
  override def toString = game.toString
}
