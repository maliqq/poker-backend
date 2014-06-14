package de.pokerno.poker

object Comparing {
  
  def equalRanks(a: Option[Rank], b: Option[Rank]): Boolean = (a, b) match {
    case (Some(r1), Some(r2)) =>
      r1 == r2
    case _ => false
  }

  def equalKinds(a: Cards, b: Cards): Boolean = {
    if (a.size != b.size) return false

    a.zipWithIndex foreach { case (card, i) ⇒
      val otherCard = b(i)
      if (card.kind != otherCard.kind) return false
    }

    return true
  }
}

trait Comparing {
  
  def compareRanks(a: Option[Rank.Value], b: Option[Rank.Value]): Int = (a, b) match {
    case (Some(r1), Some(r2)) =>
      r1 compare r2
    case (Some(_), None) =>   1
    case (None, None) =>      0
    case (_, _) =>           -1
  }

  def compareCards(a: Cards, b: Cards): Int = if (a.size == b.size) {
    var result = 0
    a.zipWithIndex.takeWhile { case (card, i) ⇒
      result = card compare b(i)
      result == 0
    }
    result
  } else {
    val l = List(a.size, b.size).min
    compareCards(a.take(l), b.take(l))
  }

}
