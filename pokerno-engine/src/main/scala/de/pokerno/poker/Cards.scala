package de.pokerno.poker

object Cards {

  def empty: Cards = Seq.empty
  
  def fromBinary(b: Array[Byte]): Cards = Array(b).map(Card(_))
  def fromSeq(l: Seq[_]): Cards = l.map(Card(_))

  def fromString(s: String): Cards = {
    val regex = """(?i)([akqjt2-9]{1})([shdc]{1})""".r
    val matching = for {
      regex(kind, suit) ← regex findAllIn s
    } yield Card(kind.charAt(0), suit.charAt(0))
    matching.toSeq
  }

}

class MaskedCards(value: Array[Byte], hidden: Boolean) {
  private var _cards: Array[Byte] = value.map { c =>
    if (hidden) Bits.makeHidden(c)
    else c
  }
  
  def :+(value: Array[Byte], hidden: Boolean) = {
    _cards ++= value.map { c =>
      if (hidden) Bits.makeHidden(c)
      else c
    }
    this
  }
  
  def /:(indexes: Array[Int]) {
    _cards = _cards.zipWithIndex.map { case (c, i) =>
      if (indexes.contains(i)) Bits.makeVisible(c)
      else c
    }
  }
  
  def masked: Array[Byte] = _cards.map { b =>
    if (Bits.isVisible(b)) b
    else 0.toByte
  }
}
