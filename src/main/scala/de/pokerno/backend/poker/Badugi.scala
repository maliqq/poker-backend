package de.pokerno.backend.poker

trait BadugiHand {
  self: Hand.Cards ⇒
  def isBadugiOne: Option[Hand] =
    if (groupKind.size == 1) {
      new Hand(self, value = value take (1)) ranked Rank.Badugi.BadugiOne
    } else if (groupSuit.size == 1) {
      val card = groupSuit.values.head min

      new Hand(self, value = List(card)) ranked Rank.Badugi.BadugiOne
    } else
      None

  def isBadugiFour: Option[Hand] =
    if (groupKind.size == 4 && groupSuit.size == 4)
      new Hand(self, value = value) ranked Rank.Badugi.BadugiFour
    else
      None

  def isBadugiThree: Option[Hand] = {
    val _paired = paired get (2)
    val _suited = suited get (2)

    if (_paired.isEmpty && _suited.isEmpty ||
        _suited.isDefined && _suited.get.size != 1 ||
        _paired.isDefined && _paired.get.size != 1)
      return None

    val (a: Card, b: Card, c: Card) = if (_paired.isDefined) {

      val v = _paired.get head
      val d = value diff (v)

      var _a = v head
      val List(_b, _c, _*) = d.filter { card ⇒ _a.kind != card.kind }
      if (_b.suit == _c.suit)
        return None

      if (_a.suit == _b.suit || _a.suit == _c.suit)
        _a = v(1)

      (_a, _b, _c)

    } else {

      val v = _suited.get head
      val d = value diff (v)

      val _a = v min

      val List(_b, _c, _*) = d.filter { card ⇒ _a.suit != card.suit }

      if (_b.kind == _c.kind)
        return None

      (_a, _b, _c)

    }
    
    new Hand(self, value = List(a, b, c)) ranked Rank.Badugi.BadugiThree
  }

  def isBadugiTwo: Option[Hand] = {
    val sets = paired get (3)

    val (a: Card, b: Card) = if (sets.isDefined) {
      val v = sets.get head
      val d = value diff (v)
      val _a = d head
      
      (_a, v.filter { card ⇒ _a.suit != card.suit } head)

    } else if (suited contains (3)) {

      val _suited = suited(3)

      val v = _suited head
      val d = value diff (v)
      val _a = d head

      (_a, v.filter { card ⇒ _a.kind != card.kind } min)

    } else if (groupSuit.size > 0) {

      val v = groupSuit.values.head
      val d = value diff (v)
      val _a = v min

      (_a, d.filter { card ⇒ _a.suit != card.suit && _a.kind != card.kind } min)

    } else {

      val v = groupKind(0)
      val d = value diff (v)
      val _a = v head

      (_a, d.filter { card ⇒ _a.kind != card.kind } min)
    }
    new Hand(self, value = List(a, b)) ranked Rank.Badugi.BadugiTwo
  }

  @throws[Hand.InvalidCards]
  def isBadugi: Option[Hand] = {
    if (value.size != 4) throw Hand.InvalidCards("4 cards required to detect badugi hand")

    isBadugiOne orElse isBadugiFour orElse isBadugiThree orElse isBadugiTwo
  }
}
