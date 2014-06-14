package de.pokerno.poker

private[poker] trait BadugiHand { self: CardSet ⇒
  
  import Rank.Badugi._
  
  def isBadugiOne: Option[Hand] =
    if (groupKind.size == 1) {
      hand(value = value take (1)).map(_.ranked(BadugiOne))
    } else if (groupSuit.size == 1) {
      val card = groupSuit.values.head.min

      hand(value = List(card)).map(_.ranked(BadugiOne))
    } else None

  def isBadugiFour: Option[Hand] =
    if (groupKind.size == 4 && groupSuit.size == 4)
      hand(value = value).map(_.ranked(BadugiFour))
    else None

  def isBadugiThree: Option[Hand] = {
    val _paired = paired get 2
    val _suited = suited get 2

    if (_paired.isEmpty && _suited.isEmpty ||
      _suited.map { _.size != 1 }.getOrElse(false) ||
      _paired.map { _.size != 1 }.getOrElse(false))
      return None
    
    val pairedThree = (v: Cards) => {
      val a = v.head
      val d = value diff v
      val Seq(b, c, _*) = d.filter { a.kind != _.kind }
      
      if (b.suit == c.suit)
        return None

      (if (a.suit == b.suit || a.suit == c.suit) v(1) else a, b, c)
    }: Card3
    
    val suitedThree = (v: Cards) => {
      val d = value diff v
      val a = v.min
      val Seq(b, c, _*) = d.filter { a.suit != _.suit }

      if (b.kind == c.kind)
        return None

      (a, b, c)
    }: Card3

    val (a: Card, b: Card, c: Card) = _paired.map { case (v::_) =>
      pairedThree(v)
    } getOrElse {
      suitedThree(_suited.get.head)
    }

    hand(value = List(a, b, c)).map(_.ranked(BadugiThree))
  }

  def isBadugiTwo: Option[Hand] = {
    val sets = paired get 3
    
    def threeKind(v: Cards): Card2 = {
      val d = value diff v
      val a = d.head
      val b = v.find { a.suit != _.suit }.get
      
      (a, b)
    }
    
    def threeFlush(_suited: Seq[Cards]): Card2 = {
      val v = _suited.head
      val d = value diff v
      val a = d.head
      val b = v.filter { a.kind != _.kind }.min

      (a, b)
    }
    
    def twoKind(v: Cards): Card2 = {
      val d = value diff v
      val a = v.head
      val b = d.filter { a.kind != _.kind }.min
      
      (a, b)
    }
    
    def twoFlush(v: Cards): Card2 = {
      val d = value diff v
      val a = v.min
      val b = d.filter { card ⇒ a.suit != card.suit && a.kind != card.kind }.min

      (a, b)
    }

    val (a: Card, b: Card) = sets.map { case (v :: _) =>
      threeKind(v)
    } getOrElse {
      suited.get(3).map {
        threeFlush(_)
      } getOrElse {
        if (groupSuit.size > 0)   twoFlush(groupSuit.values.head)
        else                      twoKind(groupKind(0))
      }
    }
    
    hand(value = List(a, b)).map(_.ranked(BadugiTwo))
  }

  @throws[Hand.InvalidCards]
  def isBadugi: Option[Hand] = {
    if (value.size != 4) throw Hand.InvalidCards("4 cards required to detect badugi hand")

    isBadugiOne orElse isBadugiFour orElse isBadugiThree orElse isBadugiTwo
  }
}
