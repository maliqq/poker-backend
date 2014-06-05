package de.pokerno.gameplay

abstract class Stage {
  val ctx: stg.Context
  def apply(): Unit
}

object Stage {
  trait Control

  case object Next extends Control {
    override def toString = "next"
  }
  case object Wait extends Throwable with Control {
    override def toString = "wait"
  }
  case object Skip extends Throwable with Control {
    override def toString = "skip"
  }
  case object Exit extends Throwable with Control {
    override def toString = "exit"
  }

}
