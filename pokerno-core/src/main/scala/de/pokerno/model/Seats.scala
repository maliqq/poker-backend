package de.pokerno.model

object Seats {

  implicit def seats2list(v: Seats): List[Seat] = v.seats
  implicit def seats2tuplelist(v: Seats): List[Tuple2[Seat, Int]] = v.seats.zipWithIndex
  
}

class Seats(val seats: List[Seat]) {
  
  type Slice = List[Tuple2[Seat, Int]]
  
  import de.pokerno.util.ConsoleUtils._
  
  def slice(pos: Int): Slice = {
    val (before, after) = seats.zipWithIndex span (_._2 <= pos)
    after ++ before
  }
  
  override def toString = seats.zipWithIndex map {
    case (seat, index) ⇒
      "Seat %d: %s" format(index, seat.toString())
  } mkString "\n"
  
}
