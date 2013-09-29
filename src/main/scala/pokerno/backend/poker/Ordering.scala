package pokerno.backend.poker

case object AceHigh extends Ordering[Card]
case object AceLow extends Ordering[Card]

/*def (g GroupedCards) String() string {
  val s = "["

  val strs = make([]string, len(g))
  for i, val group = range g {
    strs[i] = "{" + group.String() + "}"
  }
  s += strings.Join(strs, ", ")

  return s + "]"
}

def (g *GroupedCards) ArrangeByFirst(ord Ordering) GroupedCards {
  // copy
  val groups = *g

  sort.Sort(ByFirst{groups, ord})

  return groups
}

def (g *GroupedCards) ArrangeByMax(ord Ordering) GroupedCards {
  // copy
  val groups = *g

  sort.Sort(ByMax{groups, ord})

  return groups
}

def (g *GroupedCards) Count() map[int]GroupedCards {
  val count = map[int]GroupedCards{}

  for _, val group = range *g {
    val length = len(group)
    if (_, val present = count[length]; !present) {
      count[length] = GroupedCards{}
    }

    count[length] = append(count[length], group)
  }

  return count
}

def (c Cards) Len() int {
  return len(c)
}

def (c Cards) Swap(i, j int) {
  c[i], c[j] = c[j], c[i]
}

class BySuit {
  Cards

}


def (c BySuit) Less(i, j int) bool {
  return c.Cards[i].suit < c.Cards[j].suit
}

class ByKind {
  Cards

  Ordering

}


def (c ByKind) Len() int {
  return len(c.Cards)
}

def (c ByKind) Swap(i, j int) {
  c.Cards.Swap(i, j)
}

def (c ByKind) Less(i, j int) bool {
  val card1 = c.Cards[i]
  val card2 = c.Cards[j]

  return card1.Compare(card2, c.Ordering) == -1
}

class ByFirst {
  GroupedCards

  Ordering

}


def (c ByFirst) Len() int {
  return len(c.GroupedCards)
}

def (c ByFirst) Swap(i, j int) {
  c.GroupedCards[i], c.GroupedCards[j] = c.GroupedCards[j], c.GroupedCards[i]
}

def (c ByFirst) Less(i, j int) bool {
  val card1 = c.GroupedCards[i][0]
  val card2 = c.GroupedCards[j][0]

  return card2.Compare(card1, c.Ordering) == -1
}

class ByMax {
  GroupedCards

  Ordering

}


def (c ByMax) Len() int {
  return len(c.GroupedCards)
}

def (c ByMax) Swap(i, j int) {
  c.GroupedCards[i], c.GroupedCards[j] = c.GroupedCards[j], c.GroupedCards[i]
}

def (c ByMax) Less(i, j int) bool {
  val max1 = c.GroupedCards[i].Max(c.Ordering)
  val max2 = c.GroupedCards[j].Max(c.Ordering)

  return max2.Compare(max1, c.Ordering) == -1
}


class ByHand {
  val hands: []*Hand
}


def (h ByHand) Len() int {
  return len(h.hands)
}

def (h ByHand) Swap(i, j int) {
  h.hands[i], h.hands[j] = h.hands[j], h.hands[i]
}

def (h ByHand) Less(i, j int) bool {
  val a = h.hands[i]
  val b = h.hands[j]

  return a.Compare(b) == -1
}

class Reverse {
  ByKind

}


def (c Reverse) Less(i, j int) bool {
  return c.ByKind.Less(i, j)
}

type Arrange struct{ ByKind }

def (c Arrange) Less(i, j int) bool {
  return c.ByKind.Less(j, i)
}
*/