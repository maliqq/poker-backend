package pokerno.backend.engine

import pokerno.backend.model._

class Street(val name: Street.Value, val stages: List[Function1[Gameplay, Unit]]) {
  def run(gameplay: Gameplay) {
    Console.printf("= street %s start\n", name)
    for (stage <- stages) {
      stage(gameplay)
    }
  }
}

object Street {
  trait Value {
    def apply(stages: List[Function1[Gameplay, Unit]]): Street = new Street(this, stages)
  }
  
  case object Preflop extends Value {
    override def toString = "preflop"
  }
  case object Flop extends Value {
    override def toString = "flop"
  }
  case object Turn extends Value {
    override def toString = "turn"
  }
  case object River extends Value {
    override def toString = "river"
  }
  
  case object Second extends Value {
    override def toString = "second"
  }
  case object Third extends Value {
    override def toString = "third"
  }
  case object Fourth extends Value {
    override def toString = "fourth"
  }
  case object Fifth extends Value {
    override def toString = "fifth"
  }
  case object Sixth extends Value {
    override def toString = "sixth"
  }
  case object Seventh extends Value {
    override def toString = "seventh"
  }
  
  case object Predraw extends Value {
    override def toString = "predraw"
  }
  case object Draw extends Value {
    override def toString = "draw"
  }
  case object FirstDraw extends Value {
    override def toString = "first-draw"
  }
  case object SecondDraw extends Value {
    override def toString = "second-draw"
  }
  case object ThirdDraw extends Value {
    override def toString = "third-draw"
  }
}

object Streets {
  type stage = Function1[Gameplay, Unit]
  
  val dealing = new stage {
    def apply(gameplay: Gameplay) = {
      Console.printf("*** [dealing] start...\n")
    }
  }
  val betting = new stage {
    def apply(gameplay: Gameplay) = {
      Console.printf("*** [betting] start...\n")
    }
  }
  val discarding = new stage {
    def apply(gameplay: Gameplay) = {
      Console.printf("*** [discarding] start...\n")
    }
  }
  
  def betting(bigBets: Boolean) = new stage {
    def apply(gameplay: Gameplay) = {
      Console.printf("*** [betting] start...\n")
    }
  }
  
  def dealing(dealType: Dealer.DealType, cardsNum: Option[Int] = None): stage = new stage {
    def apply(gameplay: Gameplay) = {
      Console.printf("*** [dealing] start...\n")
      new Dealing(dealType, cardsNum).run(gameplay)
    }
  }
  
  final val ByGameGroup: Map[Game.Group, List[Street]] = Map(
      Game.Holdem -> List(
          Street.Preflop(
              List(
                 dealing(Dealer.Hole),
                 betting
             )
          ),
          Street.Flop(
              List(
                 dealing(Dealer.Board, Some(3)),
                 betting
             )
         ),
          Street.Turn(
              List(
                 dealing(Dealer.Board, Some(1)),
                 betting(bigBets = true)
             )
         ),
          Street.River(
              List(
                 dealing(Dealer.Board, Some(1)),
                 betting
             )
          )
      ),
      
      Game.SevenCard -> List(
          Street.Second(
              List(
                 dealing(Dealer.Hole, Some(2))
             )
         ),
          Street.Third(
              List(
                 dealing(Dealer.Door, Some(1)),
                 betting
             )
         ),
          Street.Fourth(
              List(
                 dealing(Dealer.Door, Some(1)),
                 betting
             )
         ),
          Street.Fifth(
              List(
                 dealing(Dealer.Door, Some(1)),
                 betting(bigBets = true)
             )
         ),
          Street.Sixth(
              List(
                 dealing(Dealer.Door, Some(1)),
                 betting
             )
         ),
          Street.Seventh(
              List(
                 dealing(Dealer.Hole, Some(1)),
                 betting
             )
         )
      ),
      
      Game.SingleDraw -> List(
          Street.Predraw(
              List(
                 dealing(Dealer.Hole, Some(5)),
                 betting,
                 discarding
             )
         ),
          Street.Draw(
              List(
                 betting(bigBets = true),
                 discarding
             )
         )
      ),
      
      Game.TripleDraw -> List(
          Street.Predraw(
              List(
                 dealing(Dealer.Hole),
                 betting,
                 discarding
             )
         ),
          Street.FirstDraw(
              List(
                 betting,
                 discarding
             )
         ),
          Street.SecondDraw(
              List(
                 betting(bigBets = true),
                 discarding
             )
         ),
          Street.ThirdDraw(
              List(
                 betting,
                 discarding
             )
          )
      )
  )
}
