package pokerno.backend.engine

import pokerno.backend.model._

object Street {
  trait Value {
    def apply(stages: List[Stage]): Street = new Street(this, stages)
  }
  
  object Preflop extends Value
  object Flop extends Value
  object Turn extends Value
  object River extends Value
  
  object Second extends Value
  object Third extends Value
  object Fourth extends Value
  object Fifth extends Value
  object Sixth extends Value
  object Seventh extends Value
  
  object Predraw extends Value
  object Draw extends Value
  object FirstDraw extends Value
  object SecondDraw extends Value
  object ThirdDraw extends Value
}

class Street(val name: Street.Value, val stages: List[Stage]) {
  def run(context: Gameplay.Context) {
    for (stage <- stages) {
      stage.run(context)
    }
  }
}

object Streets {
  final val ByGameGroup: Map[Game.Group, List[Street]] = Map(
      Game.Holdem -> List(
          Street.Preflop(
              List(
                 new Dealing(Dealer.Hole),
                 Betting
             )
          ),
          Street.Flop(
              List(
                 new Dealing(Dealer.Board(3)),
                 Betting
             )
         ),
          Street.Turn(
              List(
                 new Dealing(Dealer.Board(1)),
                 Betting(bigBets = true)
             )
         ),
          Street.River(
              List(
                 new Dealing(Dealer.Board(1)),
                 Betting
             )
          )
      ),
      
      Game.SevenCard -> List(
          Street.Second(
              List(
                 new Dealing(Dealer.Hole(2))
             )
         ),
          Street.Third(
              List(
                 new Dealing(Dealer.Door(1)),
                 Betting
             )
         ),
          Street.Fourth(
              List(
                 new Dealing(Dealer.Door(1)),
                 Betting
             )
         ),
          Street.Fifth(
              List(
                 new Dealing(Dealer.Door(1)),
                 Betting(bigBets = true)
             )
         ),
          Street.Sixth(
              List(
                 new Dealing(Dealer.Door(1)),
                 Betting
             )
         ),
          Street.Seventh(
              List(
                 new Dealing(Dealer.Hole(1)),
                 Betting
             )
         )
      ),
      
      Game.SingleDraw -> List(
          Street.Predraw(
              List(
                 new Dealing(Dealer.Hole(5)),
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
                 new Dealing(Dealer.Hole),
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
