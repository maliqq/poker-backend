package de.pokerno.protocol.msg

import de.pokerno.gameplay

object Conversions {
  
  implicit def street2wire(s: gameplay.Street.Value): StageEventSchema.StreetType = s match {
    case gameplay.Street.Preflop => StageEventSchema.StreetType.PREFLOP
    case gameplay.Street.Flop => StageEventSchema.StreetType.FLOP
    case gameplay.Street.Turn => StageEventSchema.StreetType.TURN
    case gameplay.Street.River => StageEventSchema.StreetType.RIVER
    
    case gameplay.Street.Third => StageEventSchema.StreetType.THIRD
    case gameplay.Street.Fourth => StageEventSchema.StreetType.FOURTH
    case gameplay.Street.Fifth => StageEventSchema.StreetType.FIFTH
    case gameplay.Street.Sixth => StageEventSchema.StreetType.SIXTH
    case gameplay.Street.Seventh => StageEventSchema.StreetType.SEVENTH
    
    case gameplay.Street.Predraw => StageEventSchema.StreetType.PREDRAW
    case gameplay.Street.Draw => StageEventSchema.StreetType.DRAW
    case gameplay.Street.FirstDraw => StageEventSchema.StreetType.FIRST_DRAW
    case gameplay.Street.SecondDraw => StageEventSchema.StreetType.SECOND_DRAW
    case gameplay.Street.ThirdDraw => StageEventSchema.StreetType.THIRD_DRAW
  }
  
}
