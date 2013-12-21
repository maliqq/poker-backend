package de.pokerno.poker

import de.pokerno.backend.{protocol => proto}

object Rank {
  type Value = proto.HandSchema.RankType
  
  object High {
    val HighCard: Value =        proto.HandSchema.RankType.HIGH_CARD
    val OnePair: Value =         proto.HandSchema.RankType.ONE_PAIR
    val TwoPair: Value =         proto.HandSchema.RankType.TWO_PAIR
    val ThreeKind: Value =       proto.HandSchema.RankType.THREE_KIND
    val Straight: Value =        proto.HandSchema.RankType.STRAIGHT
    val Flush: Value =           proto.HandSchema.RankType.FLUSH
    val FullHouse: Value =       proto.HandSchema.RankType.FULL_HOUSE
    val FourKind: Value =        proto.HandSchema.RankType.FOUR_KIND
    val StraightFlush: Value =   proto.HandSchema.RankType.STRAIGHT_FLUSH
    
    def values = List(HighCard, OnePair, TwoPair, ThreeKind, Straight, Flush, FullHouse, FourKind, StraightFlush)
  }

  object Badugi {
    val BadugiOne: Value =       proto.HandSchema.RankType.BADUGI1
    val BadugiTwo: Value =       proto.HandSchema.RankType.BADUGI2
    val BadugiThree: Value =     proto.HandSchema.RankType.BADUGI3
    val BadugiFour: Value =      proto.HandSchema.RankType.BADUGI4
    
    def values = List(BadugiOne, BadugiTwo, BadugiThree, BadugiFour)
  }
  
  object Low {
    val Complete: Value =        proto.HandSchema.RankType.LOW
    val Incomplete: Value =      proto.HandSchema.RankType.NOT_LOW
  }
  
}
