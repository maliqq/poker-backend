package de.pokerno.form.tournament

object States {
  trait Value

  // waiting players
  case object Waiting extends Value
  
  // seating
  case object Seating extends Value
  
  // play is active
  case object Running extends Value
  
  // paused - shootout next round wait, break, addon break, bubble, final table prize split
  case object Paused extends Value
  
}
