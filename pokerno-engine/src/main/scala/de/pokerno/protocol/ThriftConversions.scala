package de.pokerno.protocol

import de.pokerno.model._
import de.pokerno.poker._

object ThriftConversions {
  
  import java.nio.ByteBuffer

  implicit def hand2thrift(hand: Hand): thrift.Hand = thrift.Hand(
    cards = ByteBuffer.wrap(hand.cards.value),
    value = ByteBuffer.wrap(hand.value),
    high = Some(ByteBuffer.wrap(hand.high)),
    kicker = Some(ByteBuffer.wrap(hand.kicker)),
    rank = hand.rank map { rank =>
      rank match {
        case Rank.High.HighCard => thrift.RankType.HighCard
        case Rank.High.OnePair => thrift.RankType.OnePair
        case Rank.High.TwoPair => thrift.RankType.TwoPair
        case Rank.High.ThreeKind => thrift.RankType.ThreeKind
        case Rank.High.Straight => thrift.RankType.Straight
        case Rank.High.Flush => thrift.RankType.Flush
        case Rank.High.FullHouse => thrift.RankType.FullHouse
        case Rank.High.FourKind => thrift.RankType.FourKind
        case Rank.High.StraightFlush => thrift.RankType.StraightFlush
        
        case Rank.Badugi.BadugiOne => thrift.RankType.Badugi1
        case Rank.Badugi.BadugiTwo => thrift.RankType.Badugi2
        case Rank.Badugi.BadugiThree => thrift.RankType.Badugi3
        case Rank.Badugi.BadugiFour => thrift.RankType.Badugi4
      }
    } getOrElse(null)
  )
  
}