package pokerno.backend.engine

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
}
