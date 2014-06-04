package de.pokerno.gameplay

abstract class Stage {
  val ctx: stg.Context
  def apply(): Unit
}

object Stage {
  trait Control

  case object Next extends Control
  case object Wait extends Throwable with Control
  case object Skip extends Throwable with Control
  case object Exit extends Throwable with Control

}
