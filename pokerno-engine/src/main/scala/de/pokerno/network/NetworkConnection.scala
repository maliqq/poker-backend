package de.pokerno.network

trait NetworkConnection {

  def remoteAddr: String

  def send(msg: Any)
  def close()

}
