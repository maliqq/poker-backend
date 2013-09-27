package pokerno.backend.engine

import scala.reflect.runtime.universe._
import pokerno.backend.model._
import pokerno.backend.poker.Card
import pokerno.backend.protocol._

class Context(
    val dealer: Dealer,
    val game: Game,
    val pot: Pot,
    val stake: Stake,
    val table: Table) {
}

trait Stage {
  def run(context: Context)
}

class StageContainer(var context: Context = null) extends Runnable {
  this : Stage =>
    def run: Unit = run(context)
}

object Betting extends Stage {
  def apply(bigBets: Boolean): Stage = {
    // TODO
    this
  }
  
  def run(context: Context) {
  }
}
object Discarding extends Stage {
  def run(context: Context) {
  }
}

class Dealing(private var _dealType: Deal.Value) extends Stage {
  def run(context: Context) {
    _dealType match {
    case Deal.Hole | Deal.Door =>
      var n: Int = _dealType.cardsNum.getOrElse(0)
      if (n == 0)
        n = context.game.options.pocketSize
      
      var seats: List[Tuple2[Int, Seat]] = List()
      seats foreach { case (pos, seat) =>
        val message = new Message.DealCards(
            dealing = _dealType,
            pos = Some(pos),
            cards = context.dealer.dealPocket(_dealType, n, seat.player.get)
        )
      }
      
    case Deal.Board =>
      val message = new Message.DealCards(
          dealing = _dealType,
          pos = None,
          cards = context.dealer.dealBoard(_dealType.cardsNum.get)
      )
    }
  }
}

class PostAntes extends Stage {
  def run(context: Context) {
    val seats: List[Tuple2[Int, Seat]] = List.empty
    seats foreach {case (pos, seat) =>
      val bet = Bet.force[Bet.Ante](context.stake)
    }
  }
}

class BringIn extends Stage {
  def run(context: Context) {
    
  }
}

class BettingComplete extends Stage {
  def run(context: Context) {
    val total = context.pot.total
  }
}

object Stages {
  val Default = List(
      
  )
}

object Streets {
  val ByGameGroup: Map[Game.Group, List[Street]] = Map(
      Game.Holdem -> List(
          Street.Preflop(
              List(
                 new Dealing(Deal.Hole),
                 Betting
             )
          ),
          Street.Flop(
              List(
                 new Dealing(Deal.Board(3)),
                 Betting
             )
         ),
          Street.Turn(
              List(
                 new Dealing(Deal.Board(1)),
                 Betting(bigBets = true)
             )
         ),
          Street.River(
              List(
                 new Dealing(Deal.Board(1)),
                 Betting
             )
          )
      ),
      
      Game.SevenCard -> List(
          Street.Second(
              List(
                 new Dealing(Deal.Hole(2))
             )
         ),
          Street.Third(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting
             )
         ),
          Street.Fourth(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting
             )
         ),
          Street.Fifth(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting(bigBets = true)
             )
         ),
          Street.Sixth(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting
             )
         ),
          Street.Seventh(
              List(
                 new Dealing(Deal.Hole(1)),
                 Betting
             )
         )
      ),
      
      Game.SingleDraw -> List(
          Street.Predraw(
              List(
                 new Dealing(Deal.Hole(5)),
                 Betting,
                 Discarding
             )
         ),
          Street.Draw(
              List(
                 Betting(bigBets = true),
                 Discarding
             )
         )
      ),
      
      Game.TripleDraw -> List(
          Street.Predraw(
              List(
                 new Dealing(Deal.Hole),
                 Betting,
                 Discarding
             )
         ),
          Street.FirstDraw(
              List(
                 Betting,
                 Discarding
             )
         ),
          Street.SecondDraw(
              List(
                 Betting(bigBets = true),
                 Discarding
             )
         ),
          Street.ThirdDraw(
              List(
                 Betting,
                 Discarding
             )
          )
      )
  )
}
