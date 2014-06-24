package de.pokerno

package object poker {

  // TODO
  //type Card = Byte
  
  type Card2 = Tuple2[Card, Card]
  type Card3 = Tuple3[Card, Card, Card]
  type Card4 = Tuple4[Card, Card, Card, Card]

  final val Kinds: List[Kind.Value.Kind] =
    Kind.Value.values.toList.asInstanceOf[List[Kind.Value.Kind]]
  
  final val Suits: List[Suit.Value] =
    List(Suit.Spade, Suit.Heart, Suit.Diamond, Suit.Club)
  
  final val All: Cards = for {
    kind ← Kinds;
    suit ← Suits
  } yield new Card(kind, suit)

  def cards(s: String) = Cards.fromString(s)
  def cards(a: Seq[_]) = Cards.fromSeq(a)
  
  type Cards = Seq[Card]
  
  implicit def cards2binary(v: Cards): Array[Byte] = if (v == null) null else v.map(_.toByte).toArray

}
