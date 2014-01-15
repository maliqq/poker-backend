package de.pokerno.format.text

object Lexer {
  class Variation
  class Limit
  class BetType
  
  object Tag {
    class Table(uuid: String, max: Int)
    class Seat(playerUuid: String, stack: Int)
    class Stake(sb: Int, bb: Int, ante: Option[Int] = None)
    class Game(variation: Variation, limit: Limit)
    class Button(pos: Int)
    class Street(name: String)
    class Bet(`type`: BetType, player: String)
    class Sb(player: String)
    class Bb(player: String)
    class Ante(player: String)
    class Raise(player: String, amount: Int)
    class AllIn(player: String)
    class Call(player: String, amount: Int)
    class Fold(player: String)
    class Deal(player: String, cardsNum: Int, cards: String)
    class Json(data: String)
  }
}
