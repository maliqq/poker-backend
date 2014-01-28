package de.pokerno.model

object Seats {

  implicit def seats2list(v: Seats): List[Seat] = v.seats
  implicit def seats2tuplelist(v: Seats): List[Tuple2[Seat, Int]] = v.seats.zipWithIndex
  
}

class Seats(val seats: List[Seat]) {
  
  type Slice = List[Tuple2[Seat, Int]]
  
  def slice(pos: Int): Slice = {
    val (left, right) = seats.zipWithIndex span (_._2 != pos)
    right ++ left
  }
  
  override def toString = seats.zipWithIndex map {
    case (seat, index) ⇒
      "Seat %d: %s" format(index, seat.toString())
  } mkString "\n"
  
}