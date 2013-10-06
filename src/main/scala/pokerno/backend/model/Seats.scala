package pokerno.backend.model

class Seats(size: Int) {
  type Item = Tuple2[Seat, Int]
  
  val value = List.fill(size) { new Seat }
  
  implicit def seats2slice(v: List[Item]) = new Slice(v)
  
  def from(pos: Int): Slice = {
    val (left, right) = value.zipWithIndex span(_._2 == pos)
    val current = (value(pos), pos)
    right ++ left ++ List(current)
  }
  
  def where(f: (Seat => Boolean)) = {
    value.zipWithIndex filter { case (seat, pos) => f(seat) }
  }
  
  def apply(pos: Int) = value(pos)

  class Slice(val value: List[Item]) {
    def where(f: (Seat => Boolean)) = {
      value filter { case (seat, pos) => f(seat) }
    }
  }
}
