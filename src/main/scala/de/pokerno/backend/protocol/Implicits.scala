package de.pokerno.backend.protocol

import de.pokerno.poker
import de.pokerno.model
import com.dyuproject.protostuff

object Implicits {
  
  implicit def modelBetType2BetType(v: model.Bet.Value): BetSchema.BetType = v match {
    case model.Bet.Call => BetSchema.BetType.CALL
    case model.Bet.Raise => BetSchema.BetType.RAISE
    case model.Bet.Check => BetSchema.BetType.CHECK
    case model.Bet.Fold => BetSchema.BetType.FOLD
    case model.Bet.Ante => BetSchema.BetType.ANTE
    case model.Bet.BringIn => BetSchema.BetType.BRING_IN
    case model.Bet.SmallBlind => BetSchema.BetType.SB
    case model.Bet.BigBlind => BetSchema.BetType.BB
    case model.Bet.GuestBlind => BetSchema.BetType.GUEST_BLIND
    case model.Bet.Straddle => BetSchema.BetType.STRADDLE
  }
  
  implicit def betType2ModelBetType(v: BetSchema.BetType): model.Bet.Value = v match {
    case BetSchema.BetType.CALL => model.Bet.Call
    case BetSchema.BetType.RAISE => model.Bet.Raise  
    case BetSchema.BetType.CHECK => model.Bet.Check 
    case BetSchema.BetType.FOLD => model.Bet.Fold
    case BetSchema.BetType.ANTE => model.Bet.Ante 
    case BetSchema.BetType.BRING_IN => model.Bet.BringIn 
    case BetSchema.BetType.SB => model.Bet.SmallBlind 
    case BetSchema.BetType.BB => model.Bet.BigBlind
    case BetSchema.BetType.GUEST_BLIND => model.Bet.GuestBlind
    case BetSchema.BetType.STRADDLE => model.Bet.Straddle
  }
  
  
  implicit def byteString2Cards(v: protostuff.ByteString): List[poker.Card] = v.toByteArray.map(poker.Card.wrap(_)).toList
  implicit def cards2ByteString(v: List[poker.Card]): protostuff.ByteString = protostuff.ByteString.copyFrom(v.map(_.toByte).toArray)
  
  implicit def handRank2Rank(v: poker.Rank): HandSchema.RankType = v match {
   
    case poker.Rank.Badugi.BadugiOne => HandSchema.RankType.BADUGI1
    case poker.Rank.Badugi.BadugiTwo => HandSchema.RankType.BADUGI2
    case poker.Rank.Badugi.BadugiThree => HandSchema.RankType.BADUGI3
    case poker.Rank.Badugi.BadugiFour => HandSchema.RankType.BADUGI4
    
    case poker.Rank.High.StraightFlush => HandSchema.RankType.STRAIGHT_FLUSH
    case poker.Rank.High.FourKind => HandSchema.RankType.FOUR_KIND
    case poker.Rank.High.FullHouse => HandSchema.RankType.FULL_HOUSE
    case poker.Rank.High.Flush => HandSchema.RankType.FLUSH
    case poker.Rank.High.Straight => HandSchema.RankType.STRAIGHT
    case poker.Rank.High.ThreeKind => HandSchema.RankType.THREE_KIND
    case poker.Rank.High.TwoPair => HandSchema.RankType.TWO_PAIR
    case poker.Rank.High.OnePair => HandSchema.RankType.ONE_PAIR
    case poker.Rank.High.HighCard => HandSchema.RankType.HIGH_CARD
    
    case poker.Rank.Low.Complete => HandSchema.RankType.LOW
    case poker.Rank.Low.Incomplete => HandSchema.RankType.NOT_LOW
    
  }
  
  implicit def rank2HandRank(v: HandSchema.RankType): poker.Rank = v match {
    case HandSchema.RankType.BADUGI1 => poker.Rank.Badugi.BadugiOne
    case HandSchema.RankType.BADUGI2 => poker.Rank.Badugi.BadugiTwo
    case HandSchema.RankType.BADUGI3 => poker.Rank.Badugi.BadugiThree
    case HandSchema.RankType.BADUGI4 => poker.Rank.Badugi.BadugiFour
    
    case HandSchema.RankType.STRAIGHT_FLUSH => poker.Rank.High.StraightFlush
    case HandSchema.RankType.FOUR_KIND => poker.Rank.High.FourKind
    case HandSchema.RankType.FULL_HOUSE => poker.Rank.High.FullHouse
    case HandSchema.RankType.FLUSH => poker.Rank.High.Flush
    case HandSchema.RankType.STRAIGHT => poker.Rank.High.Straight
    case HandSchema.RankType.THREE_KIND => poker.Rank.High.ThreeKind
    case HandSchema.RankType.TWO_PAIR => poker.Rank.High.TwoPair
    case HandSchema.RankType.ONE_PAIR => poker.Rank.High.OnePair
    case HandSchema.RankType.HIGH_CARD => poker.Rank.High.HighCard
    
    case HandSchema.RankType.LOW => poker.Rank.Low.Complete
    case HandSchema.RankType.NOT_LOW => poker.Rank.Low.Incomplete
  }

}