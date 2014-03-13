package de.pokerno.model

object Seats {

  implicit def seats2list(v: Seats): List[Seat] = v.seats.toList
  implicit def seats2tuplelist(v: Seats): List[Tuple2[Seat, Int]] = v.seats.toList.zipWithIndex

}

trait SliceableSeq[T] {

  def seq: Seq[T]

  def zipped = seq.zipWithIndex

  def slice(from: Int): Seq[Tuple2[T, Int]] = {
    val (before, after) = zipped span (_._2 <= from)
    after ++ before
  }

}

class Seats(val seats: collection.mutable.LinearSeq[Seat]) extends SliceableSeq[Seat] {

  def seq = seats

  def apply(at: Int) = seats(at)

  override def toString = seats.zipWithIndex map {
    case (seat, index) ⇒
      "Seat %d: %s" format (index, seat.toString())
  } mkString "\n"

  def clear(pos: Int): Unit = seats(pos) = new Seat

}
