package de.pokerno.poker

import de.pokerno.backend.{protocol => proto}

object Rank {
  type Value = proto.HandSchema.RankType
  
  object High {
    final val HighCard: Value =        proto.HandSchema.RankType.HIGH_CARD
    final val OnePair: Value =         proto.HandSchema.RankType.ONE_PAIR
    final val TwoPair: Value =         proto.HandSchema.RankType.TWO_PAIR
    final val ThreeKind: Value =       proto.HandSchema.RankType.THREE_KIND
    final val Straight: Value =        proto.HandSchema.RankType.STRAIGHT
    final val Flush: Value =           proto.HandSchema.RankType.FLUSH
    final val FullHouse: Value =       proto.HandSchema.RankType.FULL_HOUSE
    final val FourKind: Value =        proto.HandSchema.RankType.FOUR_KIND
    final val StraightFlush: Value =   proto.HandSchema.RankType.STRAIGHT_FLUSH
    
    def values = List(HighCard, OnePair, TwoPair, ThreeKind, Straight, Flush, FullHouse, FourKind, StraightFlush)
  }

  object Badugi {
    final val BadugiOne: Value =       proto.HandSchema.RankType.BADUGI1
    final val BadugiTwo: Value =       proto.HandSchema.RankType.BADUGI2
    final val BadugiThree: Value =     proto.HandSchema.RankType.BADUGI3
    final val BadugiFour: Value =      proto.HandSchema.RankType.BADUGI4
    
    def values = List(BadugiOne, BadugiTwo, BadugiThree, BadugiFour)
  }
  
  object Low {
    final val Complete: Value =        proto.HandSchema.RankType.LOW
    final val Incomplete: Value =      proto.HandSchema.RankType.NOT_LOW
  }
  
}
