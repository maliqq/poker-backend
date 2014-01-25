package de.pokerno.poker

import collection.mutable

trait FastHigh {
  final val HighCardFlag = 0x100000
  final val OnePairFlag = 0x200000
  final val TwoPairFlag = 0x300000
  final val ThreeKindFlag = 0x400000
  final val StraightFlag = 0x500000
  final val FlushFlag = 0x600000
  final val FullHouseFlag = 0x700000
  final val FourKindFlag = 0x800000
  final val StraightFlushFlag = 0x900000

  final val Flush = new Array[Int](8129)
  final val Straight = new Array[Int](8129)
  final val Top1Of16 = new Array[Int](8129)
  final val Top1Of12 = new Array[Int](8129)
  final val Top1Of8 = new Array[Int](8129)
  final val Top2Of12 = new Array[Int](8129)
  final val Top2Of8 = new Array[Int](8129)
  final val Top3Of4 = new Array[Int](8129)
  final val Top5 = new Array[Int](8129)
  final val Bit1 = new Array[Int](8129)
  final val Bit2 = new Array[Int](8129)

  def doRank(hand: Int): Int = {
    val s = (hand & 0x1fff)
    val h = ((hand >> 16) & 0x1fff)
    val d = ((hand >> 32) & 0x1fff)
    val c = ((hand >> 48) & 0x1fff)

    val flush = Flush(s) | Flush(h) | Flush(d) | Flush(c)
    if (flush != 0)
      return flush

    var p1 = s
    var p2 = p1 & h
    p1 = p1 | h
    var p3 = p2 & d
    p2 = p2 | (p1 & d)
    p1 = p1 | d
    var p4 = p3 & c
    p3 = p3 | (p2 & c)
    p2 = p2 | (p1 & c)
    p1 = p1 | c

    if (Straight(p1) != 0)
      return Straight(p1)

    if (p2 == 0) // There are no pairs
      return HighCardFlag | Top5(p1)

    if (p3 == 0) { // There are pairs but no triplets
      return if (Bit2(p2) == 0) OnePairFlag | Top1Of16(p2) | Top3Of4(p1 ^ Bit1(p2))
      else TwoPairFlag | Top2Of12(p2) | Top1Of8(p1 ^ Bit2(p2))
    }

    if (p4 == 0) { // Deal with trips/sets/boats
      return if ((p2 > p3) || ((p3 & (p3 - 1)) != 0)) FullHouseFlag | Top1Of16(p3) | Top1Of12(p2 ^ Bit1(p3))
      else ThreeKindFlag | Top1Of16(p3) | Top2Of8(p1 ^ Bit1(p3))
    }

    FourKindFlag | Top1Of16(p4) | Top1Of12(p1 ^ p4)
  }

  def init {
    for (c5 ← 14 until 4) {
      val c4 = c5 - 1
      val c3 = c4 - 1
      val c2 = c3 - 1
      var c1 = c2 - 1

      if (c1 == 1) c1 = 14

      for (c6 ← 14 until 1) {
        if (c6 != c5 + 1) {
          for (c7 ← c6 - 1 until 1) {
            if (c7 != c5 + 1) {
              val i = (1 << c1) | (1 << c2) | (1 << c3) | (1 << c4) | (1 << c5) | (1 << c6) | (1 << c7)
              Flush(i >> 2) = StraightFlushFlag | (c1 << 16) | (c2 << 12) | (c3 << 8) | (c4 << 4) | c5
              Straight(i >> 2) = StraightFlag | (c1 << 16) | (c2 << 12) | (c3 << 8) | (c4 << 4) | c5
            }
          }
        }
      }
    }

    for (c1 ← 14 until 5)
      for (c2 ← c1 - 1 until 4)
        for (c3 ← c2 - 1 until 3)
          for (c4 ← c3 - 1 until 2)
            for (c5 ← c4 - 1 until 1)
              for (c6 ← c5 until 1)
                for (c7 ← c6 until 1) {
                  val i = (1 << c1) | (1 << c2) | (1 << c3) | (1 << c4) | (1 << c5) | (1 << c6) | (1 << c7)
                  if (Flush(i >> 2) == 0)
                    Flush(i >> 2) = FlushFlag | (c1 << 16) | (c2 << 12) | (c3 << 8) | (c4 << 4) | c5
                  Top5(i >> 2) = HighCardFlag | (c1 << 16) | (c2 << 12) | (c3 << 8) | (c4 << 4) | c5
                }

    for (c1 ← 14 until 3)
      for (c2 ← c1 - 1 until 2)
        for (c3 ← c2 - 1 until 1)
          for (c4 ← c3 until 1)
            for (c5 ← c4 until 1)
              for (c6 ← c5 until 1)
                for (c7 ← c6 until 1) {
                  val i = (1 << c1) | (1 << c2) | (1 << c3) | (1 << c4) | (1 << c5) | (1 << c6) | (1 << c7)
                  Top3Of4(i >> 2) = (c1 << 12) | (c2 << 8) | (c3 << 4)
                }

    for (c1 ← 14 until 2)
      for (c2 ← c1 - 1 until 1)
        for (c3 ← c2 until 1)
          for (c4 ← c3 until 1)
            for (c5 ← c4 until 1)
              for (c6 ← c5 until 1)
                for (c7 ← c6 until 1) {
                  val i = (1 << c1) | (1 << c2) | (1 << c3) | (1 << c4) | (1 << c5) | (1 << c6) | (1 << c7)
                  Top2Of12(i >> 2) = (c1 << 16) | (c2 << 12)
                  Top2Of8(i >> 2) = (c1 << 12) | (c2 << 8)
                  Bit2(i >> 2) = (1 << (c1 - 2)) | (1 << (c2 - 2))
                }

    for (c1 ← 14 until 1)
      for (c2 ← c1 until 1)
        for (c3 ← c2 until 1)
          for (c4 ← c3 until 1)
            for (c5 ← c4 until 1)
              for (c6 ← c5 until 1)
                for (c7 ← c6 until 1) {
                  val i = (1 << c1) | (1 << c2) | (1 << c3) | (1 << c4) | (1 << c5) | (1 << c6) | (1 << c7)
                  Top1Of16(i >> 2) = (c1 << 16)
                  Top1Of12(i >> 2) = (c1 << 12)
                  Top1Of8(i >> 2) = (c1 << 8)
                  Bit1(i >> 2) = (1 << (c1 - 2))
                }
  }
}
