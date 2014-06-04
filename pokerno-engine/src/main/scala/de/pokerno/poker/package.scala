package de.pokerno

package object poker {

  final val Kinds: List[Kind.Value.Kind] = Kind.Value.values.toList.asInstanceOf[List[Kind.Value.Kind]]
  final val Suits: List[Suit.Value] = List(Suit.Spade, Suit.Heart, Suit.Diamond, Suit.Club)
  
  final val All: Cards = for {
    kind ← Kinds;
    suit ← Suits
  } yield new Card(kind, suit)
  
  def cards(s: String) = Cards.fromString(s)
  def cards(a: Seq[_]) = Cards.fromSeq(a)
  
  type Cards = Seq[Card]

  implicit def cards2binary(v: Cards): Array[Byte] = v.map(_.toByte).toArray
  implicit def binary2cards(v: Array[Byte]): Cards = Cards.fromBinary(v)

}
