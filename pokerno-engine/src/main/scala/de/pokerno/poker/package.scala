package de.pokerno

package object poker {

  final val Kinds: List[Kind.Value.Kind] = Kind.Value.values.toList.asInstanceOf[List[Kind.Value.Kind]]
  final val Suits: List[Suit.Value] = List(Suit.Spade, Suit.Heart, Suit.Diamond, Suit.Club)
  
  final val Cards = for {
    kind ← Kinds;
    suit ← Suits
  } yield new Card(kind, suit)
  
  
  def Cards(s: String) = CardUtils.parseString(s)
  def Cards(a: Seq[_]) = CardUtils.parseSeq(a)

  implicit def cards2binary(v: Seq[Card]): Array[Byte] = v.map(_.toByte).toArray
  implicit def binary2cards(v: Array[Byte]): Seq[Card] = CardUtils.parseBinary(v)

}
