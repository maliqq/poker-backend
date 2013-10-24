package pokerno.backend.poker

trait BadugiHand {
  self: Hand.Cards ⇒
  def isBadugiOne: Option[Hand] =
    if (groupKind.size == 1) {
      Some(new Hand(self, value = value take (1)))
    } else if (groupSuit.size == 1) {
      val card = groupSuit(0) min

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

    if (_paired.isEmpty && _suited.isEmpty)
      return None

    val (a: Card, b: Card, c: Card) = if (_paired.size == 1 && _suited.isDefined && _suited.get.size != 2) {

      val value = _paired.get head
      val diff = value diff (value)

      var _a = value head
      val List(_b, _c, _*) = diff.filter { card ⇒ _a.kind != card.kind }
      if (_b.suit == _c.suit)
        return None

      if (_a.suit == _b.suit || _a.suit == _c.suit)
        _a = value head

      (_a, _b, _c)

    } else if (_paired.isEmpty && _suited.isDefined && _suited.get.size == 1) {

      val value = _suited.get head
      val diff = value diff (value)

      val _a = value min

      val List(_b, _c, _*) = diff.filter { card ⇒ _a.suit != card.suit }

      if (_b.kind == _c.kind)
        return None

      (_a, _b, _c)

    } else
      return None

    new Hand(self, value = List(a, b, c)) ranked Rank.Badugi.BadugiThree
  }

  def isBadugiTwo: Option[Hand] = {
    val sets = paired get (3)

    val (a: Card, b: Card) = if (sets.isDefined) {
      val value = sets.get head
      val diff = value diff (value)
      val _a = diff head

      (_a, value.filter { card ⇒ _a.suit != card.suit } head)

    } else if (suited contains (3)) {

      val _suited = suited(3)

      val value = _suited head
      val diff = value diff (value)
      val _a = diff head

      (_a, value.filter { card ⇒ _a.kind != card.kind } min)

    } else if (groupSuit.size > 0) {

      val value = groupSuit(0)
      val diff = value diff (value)
      val _a = value min

      (_a, diff.filter { card ⇒ _a.suit != card.suit && _a.kind != card.kind } min)

    } else {

      val value = groupKind(0)
      val diff = value diff (value)
      val _a = value head

      (_a, diff.filter { card ⇒ _a.kind != card.kind } min)
    }
    new Hand(self, value = List(a, b)) ranked Rank.Badugi.BadugiTwo
  }

  @throws[Hand.InvalidCards]
  def isBadugi: Option[Hand] = {
    if (value.size != 4) throw Hand.InvalidCards("4 cards required to detect badugi hand")

    isBadugiOne orElse isBadugiFour orElse isBadugiThree orElse isBadugiTwo
  }
}
