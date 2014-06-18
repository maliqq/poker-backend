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
      thrift.RankType.valueOf(rank.name.replaceAll("-", "")).get
    } orNull
  )
  
}