package pokerno.backend.poker

trait Badugi extends Hand {
  def isBadugiOne: Option[Hand] =
    if (cards.groupKind.size == 1) {
      Some(new Hand(value = cards.value.take(1)))
    } else if (cards.groupSuit.size == 1) {
      val card = cards.groupSuit(0).min

      Some(new Hand(value = List(card)))
    } else
      None

  def isBadugiFour: Option[Hand] =
    if (cards.groupKind.size == 4 && cards.groupSuit.size == 4)
      Some(new Hand(value = cards.value))
    else
      None
      
  def isBadugiThree: Option[Hand] = {
    val paired = cards.paired.get(2)
    val suited = cards.suited.get(2)

    if (paired.isEmpty && suited.isEmpty)
      return None

    val (a: Card, b: Card, c: Card) = if (paired.size == 1 && suited.isDefined && suited.get.size != 2) {
      
      val value = paired.get.head
      val diff = cards.value.diff(value)
      
      var _a = value.head
      val List(_b, _c, _*) = diff.filter { card => _a.kind != card.kind }
      if (_b.suit == _c.suit)
        return None
      
      if (_a.suit == _b.suit || _a.suit == _c.suit)
        _a = value.head
      
      (_a, _b, _c)

    } else if (paired.isEmpty && suited.isDefined && suited.get.size == 1) {
      
      val value = suited.get.head
      val diff = cards.value.diff(value)
      
      val _a = value.min
      
      val List(_b, _c, _*) = diff.filter { card => _a.suit != card.suit }
      
      if (_b.kind == _c.kind)
        return None
      
      (_a, _b, _c)

    } else
      return None

    return Some(new Hand(value = List(a, b, c)))
  }

  def isBadugiTwo: Option[Hand] ={
    val sets = cards.paired.get(3)

    val (a: Card, b: Card) = if (sets.isDefined) {
      val value = sets.get.head
      val diff = cards.value.diff(value)
      val _a = diff.head

      (_a, value.filter{ card => _a.suit != card.suit }.head)
    
    } else if (cards.suited.contains(3)) {
    
      val suited = cards.suited(3)
      
      val value = suited.head
      val diff = cards.value.diff(value)
      val _a = diff.head

      (_a, value.filter { card => _a.kind != card.kind }.min)

    } else if (cards.groupSuit.size > 0) {
      
      val value = cards.groupSuit(0)
      val diff = cards.value.diff(value)
      val _a = value.min

      (_a, diff.filter{ card => _a.suit != card.suit && _a.kind != card.kind }.min)

    } else {
    
      val value = cards.groupKind(0)
      val diff = cards.value.diff(value)
      val _a = value.head

      (_a, diff.filter { card => _a.kind != card.kind }.min)
    }
    Some(new Hand(value = List(a, b)))
  }
/*
  def isBadugi(cards *Cards) {
    if (len(*cards) != 4) {
      return nil, errors.New("4 cards required to detect badugi hand")
    }

    val hc = NewHandCards(cards, AceLow, false)

    val hand = cards.Detect(badugiRanks)

    return hand, nil
  }*/
}
