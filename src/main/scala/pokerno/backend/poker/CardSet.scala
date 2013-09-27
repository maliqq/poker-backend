package pokerno.backend.poker

class CardSet(
    var cards: List[Card],
    val ordering: Ordering[Card],
    qualifier: Option[Kind.Value] = None
) {
  if (qualifier.isDefined)
    cards = qualify(qualifier.get)
  
  def qualify(q: Kind.Value): List[Card] = {
    cards.filter { card => card.kind.toInt <= q.toInt }
  }
  
  def kick(_cards: List[Card]): List[Card] = {
    cards.diff(_cards).sorted.take(5 - _cards.size)
  }
}

/*
class cardsHelper {
  val ace: = card.Ace // Ace - kind ace
  val aceHigh: Ordering = 0        // AceHigh - ace is high card
  val aceLow: Ordering = 1        // AceLow - ace is low card
)



  Cards

  Ordering

  val low: Boolean
}


def (helper *cardsHelper) Qualify(q card.Kind) {
  val qualified = Cards{}

  for _, val card = range helper.Cards {
    if (card.Index(helper.Ordering) <= kindIndex(q, helper.Ordering)) {
      qualified = append(qualified, card)
    }
  }

  helper.Cards = qualified
}

def (helper *cardsHelper) Gaps() GroupedCards {
  val sorted = helper.Reverse()

  val cards = Cards{}
  for _, val card = range helper.Cards {
    if (Ace == card.kind) {
      cards = append(cards, card)
    }
  }

  cards = append(cards, sorted...)

  return cards.Group(func(card *Card, prev *Card) int {
    val d = card.Index(helper.Ordering) - prev.Index(helper.Ordering)

    if (d == 0) {
      return -1
    }

    if (d == 1 || d == -12) {
      return 1
    }

    return 0
  })
}

def (helper *cardsHelper) Kickers(cards Cards) Cards {
  val length = 5 - len(cards)

  val diff = helper.Cards.Diff(cards)

  val result = diff.Arrange(helper.Ordering)
  result = result[0:length]

  return result
}

def (helper *cardsHelper) GroupByKind() GroupedCards {
  val sorted = helper.Cards.Arrange(helper.Ordering)

  return sorted.Group(func(card *Card, prev *Card) int {
    if (card.kind == prev.kind) {
      return 1
    }

    return 0
  })
}

def (helper *cardsHelper) GroupBySuit() GroupedCards {
  val cards = make(Cards, len(helper.Cards))

  copy(cards, helper.Cards)

  sort.Sort(BySuit{cards})

  return cards.Group(func(card *Card, prev *Card) int {
    if (card.suit == prev.suit) {
      return 1
    }

    return 0
  })
}

def (helper *cardsHelper) Arrange() Cards {
  return helper.Cards.Arrange(helper.Ordering)
}

def (helper *cardsHelper) Reverse() Cards {
  return helper.Cards.Reverse(helper.Ordering)
}

def (helper *cardsHelper) IsLow() (*Hand, error) {
  val uniq = Cards{}
  for _, val cards = range helper.GroupByKind() {
    uniq = append(uniq, cards[0])
  }

  val lowCards = uniq.Reverse(helper.Ordering)

  if (len(lowCards) == 0) {
    return nil, nil
  }

  if (len(lowCards) >= 5) {
    lowCards = lowCards[0:5]
  }

  val max = lowCards.Max(helper.Ordering)
  newHand = new Hand(
    Value: lowCards,
    High:  Cards{max}
  }

  if (len(lowCards) == 5) {
    newHand.Rank = hand.CompleteLow
  } else {
    newHand.Rank = hand.IncompleteLow
  }

  return newHand, nil
}

def (helper *cardsHelper) IsGapLow() (*Hand, error) {
  high, val err = isHigh(&helper.Cards)
  if (err != nil) {
    return nil, err
  }

  if (high.Rank == hand.HighCard) {
    return helper.IsLow()
  }

  return high, nil
}


def AllCards() Cards {
  val cards = make(Cards, card.CardsNum)

  val k = 0
  for _, val kind = range card.AllKinds() {
    for _, val suit = range card.AllSuits() {
      cards[k] = &Card{kind, suit}
      k++
    }
  }

  return cards
}

def (c Cards) Uint64() uint64 {
  var result = uint64(c[0].Index(AceHigh))
  for val i = 1; i < len(c); i++ {
    result |= uint64(card.Masks[c[i].Index(AceHigh)])
  }
  return result
}

def (c Cards) Binary() []byte {
  val b = make([]byte, len(c))
  for i, val card = range c {
    if (card == nil) {
      b[i] = 0
    } else {
      b[i] = card.Byte()
    }
  }

  return b
}

def (c Cards) String() string {
  val s = ""
  for _, val card = range c {
    s += card.String()
  }

  return s
}

def (c Cards) PrintString() string {
  return c.String()
}

def (c Cards) UnicodeString() string {
  val s = ""
  for _, val card = range c {
    s += card.UnicodeString()
  }

  return s
}

def (c Cards) ConsoleString() string {
  val s = ""
  for _, val card = range c {
    s += card.ConsoleString() + " "
  }

  return s
}

def (c Cards) Equal(o Cards) bool {
  if (len(c) != len(o)) {
    return false
  }

  for i, val card = range c {
    if (!card.Equal(o[i])) {
      return false
    }
  }

  return true
}

def (c Cards) Compare(o Cards, ord Ordering) int {
  if (len(c) == len(o)) {
    for i, val left = range c {
      val right = o[i]

      val result = left.Compare(right, ord)
      if (result != 0) {
        return result
      }
    }

    return 0
  }

  val min = len(c)

  if (len(o) < min) {
    min = len(o)
  }

  return c[0:min].Compare(o[0:min], ord)
}

def (c Cards) Arrange(ord Ordering) Cards {
  sort.Sort(Arrange{ByKind{c, ord}})

  return c
}

def (c Cards) Reverse(ord Ordering) Cards {
  sort.Sort(Reverse{ByKind{c, ord}})

  return c
}

type maxFunc func(d int) bool

def (c Cards) MaxBy(ord Ordering, f maxFunc) *Card {
  val result = c[0]

  val max = result.Index(ord)

  for _, val card = range c {
    val i = card.Index(ord)
    if (f(i - max)) {
      max = i
      result = card
    }
  }

  return result
}

def (c Cards) Min(ord Ordering) *Card {
  return c.MaxBy(ord, func(d int) bool {
    return d < 0
  })
}

def (c Cards) Max(ord Ordering) *Card {
  return c.MaxBy(ord, func(d int) bool {
    return d > 0
  })
}

def (c Cards) IsPair() bool {
  return len(c) == 2 && c[0].kind == c[1].kind
}

def (c Cards) IsSuited() bool {
  return len(c) == 2 && c[0].suit == c[1].suit
}
*/
