package de.pokerno.backend

trait Connection {

  def remoteAddr: String

  def send(msg: Any)

}
