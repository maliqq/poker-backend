package de.pokerno.gameplay

import de.pokerno.model._
import de.pokerno.protocol.GameEvent
import de.pokerno.poker.{ Card, Deck }

case class Dealing(ctx: StageContext, _type: DealType.Value, cardsNum: Option[Int] = None) extends Stage(ctx) {
  def process() = _type match {
    case DealType.Hole | DealType.Door ⇒
      var n: Int = cardsNum getOrElse (0)
      if (n <= 0 || n > game.options.pocketSize) n = game.options.pocketSize

      Console printf("dealing %s %d cards\n", _type, n)

      table.seats.zipWithIndex filter (_._1 isActive) foreach {
        case (seat, pos) ⇒
          val player = seat.player.get
          val cards = dealer dealPocket (n, player)
          val box = Some((player, pos))

          if (_type == DealType.Hole) {
            events.publish(GameEvent.dealCards(_type, cards, box)) { _.only(player) }
            events.publish(GameEvent.dealCardsNum(_type, cards, box)) { _.except(player) }
          } else
            events.publish(GameEvent.dealCards(_type, cards, box)) { _.all() }
      }

    case DealType.Board if cardsNum.isDefined ⇒

      Console printf("dealing board %d cards\n", cardsNum.get)

      val cards = dealer dealBoard (cardsNum.get)
      events.publish(
          GameEvent.dealCards(_type, cards)) { _.all() }

    case _ ⇒
    // TODO
  }

}
//
//private[gameplay] object Dealing {
//  
//  case class DealCards(
//      _type: DealType.Value,
//      player: Option[Player] = None,
//      cards: Option[List[Card]] = None, 
//      cardsNum: Option[Int] = None)
//
//  trait ReplayContext {
//    replay: Replay ⇒
//
//    import concurrent.duration.Duration
//    import de.pokerno.util.ConsoleUtils._
//
//    def dealing(dealActions: List[Dealing.DealCards], dealOptions: Tuple2[DealType.Value, Option[Int]], speed: Duration) {
//      def sleep() = Thread.sleep(speed.toMillis)
//
//      val dealer = gameplay.dealer
//
//      val _type = dealOptions.dealType
//
//      _type match {
//        case DealType.Board ⇒
//
//          var cardsDealt: Option[List[Card]] = dealActions.headOption.map(_.cards.get)
//
//          dealCards(Dealing.DealCards(_type,
//            cards = cardsDealt,
//            cardsNum = dealOptions.cardsNum
//          ))
//          sleep()
//
//        case DealType.Door | DealType.Hole ⇒
//
//          val dealActionsByPlayer = collection.mutable.HashMap[Player, Dealing.DealCards]()
//          dealActions.foreach { action ⇒
//            dealActionsByPlayer(action.player.get) = action
//          }
//
//          t.seatsAsList.zipWithIndex filter (_._1 isActive) foreach {
//            case (seat, pos) ⇒
//              val player = seat.player.get
//              if (dealActionsByPlayer.contains(player)) {
//                val dealPocket = dealActionsByPlayer(player)
//
//                dealCards(dealPocket.copy(player = Some(player)))
//              } else {
//                val pocketSize = gameOptions.pocketSize
//
//                dealCards(Dealing.DealCards(_type,
//                  player = Some(player),
//                  cardsNum = Some(pocketSize)
//                ))
//              }
//              sleep()
//          }
//      }
//    }
//
//    def dealCards(d: Dealing.DealCards) {
//
//      val gameOptions = gameplay.game.options
//      val dealer = gameplay.dealer
//
//      d._type match {
//        case DealType.Hole | DealType.Door ⇒
//
//          val (_player: Player, pos: Int) =
//            d.player match {
//              case Some(p) ⇒
//                (p, t.playerPos(p))
//
//              case None ⇒
//                val (seat, pos) = gameplay.round.acting.get
//                (seat.player.get, pos)
//            }
//
//          val pocketSize = gameOptions.pocketSize
//
//          def dealPocket(cards: Either[Int, List[Card]], player: Player): List[Card] = {
//            val max = pocketSize - dealer.pocketOption(_player).map(_.size).getOrElse(0)
//
//            cards match {
//              case Left(n) if n > 0 ⇒
//                dealer.dealPocket(List(n, max).min, _player)
//
//              case Right(cards) if !cards.isEmpty ⇒
//                dealer.dealPocket(cards.take(max), _player)
//
//              case _ ⇒ List.empty
//            }
//          }
//
//          val cardsDealt: List[Card] = if (d.cards.isDefined && !d.cards.get.isEmpty)
//            dealPocket(Right(d.cards.get), _player)
//          else
//            dealPocket(Left(d.cardsNum.getOrElse(pocketSize)), _player)
//
//          debug(" | deal %s -> %s", cardsDealt, _player)
//          e.publish(GameEvent.dealCards(d._type, cardsDealt, Some(_player, pos))) { _.all() }
//
//        case DealType.Board if gameOptions.hasBoard ⇒
//
//          def dealBoard(cards: Either[Int, List[Card]]): List[Card] = {
//            val max = Deck.FullBoardSize - dealer.board.size
//
//            cards match {
//              case Left(n) if n > 0 ⇒
//                dealer.dealBoard(List(n, max).min)
//
//              case Right(cards) if !cards.isEmpty ⇒
//                dealer.dealBoard(cards.take(max))
//
//              case _ ⇒ List.empty
//            }
//          }
//
//          val cardsDealt: List[Card] = if (d.cards.isDefined && !d.cards.get.isEmpty)
//            dealBoard(Right(d.cards.get))
//          else
//            dealBoard(Left(d.cardsNum.getOrElse(0)))
//
//          debug(" | deal board %s", cardsDealt)
//          e.publish(GameEvent.dealCards(d._type, cardsDealt)) { _.all() }
//
//      }
//    }
//  }
//
//}