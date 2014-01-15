package de.pokerno.poker

import de.pokerno.protocol.wire

object Rank {
  type Value = wire.HandSchema.RankType
  
  object High {
    final val HighCard: Value =        wire.HandSchema.RankType.HIGH_CARD
    final val OnePair: Value =         wire.HandSchema.RankType.ONE_PAIR
    final val TwoPair: Value =         wire.HandSchema.RankType.TWO_PAIR
    final val ThreeKind: Value =       wire.HandSchema.RankType.THREE_KIND
    final val Straight: Value =        wire.HandSchema.RankType.STRAIGHT
    final val Flush: Value =           wire.HandSchema.RankType.FLUSH
    final val FullHouse: Value =       wire.HandSchema.RankType.FULL_HOUSE
    final val FourKind: Value =        wire.HandSchema.RankType.FOUR_KIND
    final val StraightFlush: Value =   wire.HandSchema.RankType.STRAIGHT_FLUSH
    
    def values = List(HighCard, OnePair, TwoPair, ThreeKind, Straight, Flush, FullHouse, FourKind, StraightFlush)
  }

  object Badugi {
    final val BadugiOne: Value =       wire.HandSchema.RankType.BADUGI1
    final val BadugiTwo: Value =       wire.HandSchema.RankType.BADUGI2
    final val BadugiThree: Value =     wire.HandSchema.RankType.BADUGI3
    final val BadugiFour: Value =      wire.HandSchema.RankType.BADUGI4
    
    def values = List(BadugiOne, BadugiTwo, BadugiThree, BadugiFour)
  }
  
  object Low {
    final val Complete: Value =        wire.HandSchema.RankType.LOW
    final val Incomplete: Value =      wire.HandSchema.RankType.NOT_LOW
  }
  
}
