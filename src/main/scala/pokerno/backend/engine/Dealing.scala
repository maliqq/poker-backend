package pokerno.backend.engine

import pokerno.backend.protocol._
import pokerno.backend.model._

class Dealing(private var _dealType: Dealer.DealType, private var cardsNum: Option[Int] = None) {
  def run(gameplay: Gameplay) {
    _dealType match {
    case Dealer.Hole | Dealer.Door =>
      var n: Int = cardsNum getOrElse(0)
      if (n == 0)
        n = gameplay.game.options.pocketSize
      
      Console.printf("dealing %s %d cards\n", _dealType, n)
      
      gameplay.table where(_ isActive) foreach { case (seat, pos) =>
        val message = Message.DealCards(
          _type = _dealType,
          pos = Some(pos),
          cards = gameplay.dealer dealPocket(_dealType, n, seat.player.get)
        )
        gameplay.broadcast all(message)
      }
      
    case Dealer.Board =>
      
      Console.printf("dealing board %d cards\n", cardsNum.get)
      
      gameplay.broadcast all(Message.DealCards(
        _type = _dealType,
        pos = None,
        cards = gameplay.dealer dealBoard(cardsNum.get)
      ))
    }
  }
}
