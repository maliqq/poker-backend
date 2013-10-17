package pokerno.backend.model

object Mix {
  final val MaxTableSize = 8
}

class Mix(val game: Game.Mixed, var _tableSize: Int = Mix.MaxTableSize) extends Variation {
  val options = Mixes.Default(game)
  if (_tableSize > Mix.MaxTableSize)
    _tableSize = Mix.MaxTableSize
  val tableSize = _tableSize
  val games = options.map { option ⇒
    new Game(option._1, Some(option._2), Some(tableSize))
  }
  override def toString = game.toString
}

object Mixes {
  val Default: Map[Game.Mixed, List[Tuple2[Game.Limited, Game.Limit]]] = Map(
    Game.Horse -> List(Game.Texas, Game.Omaha8, Game.Razz, Game.Stud, Game.Stud8).map((_, Game.FixedLimit)),
    Game.Eight -> List(
      (Game.Triple27, Game.FixedLimit),
      (Game.Texas, Game.FixedLimit),
      (Game.Omaha8, Game.FixedLimit),
      (Game.Razz, Game.FixedLimit),
      (Game.Stud, Game.FixedLimit),
      (Game.Stud8, Game.FixedLimit),
      (Game.Texas, Game.NoLimit),
      (Game.Omaha, Game.PotLimit)))
}
