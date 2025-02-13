package de.pokerno.network

trait PlayerConnection extends NetworkConnection {

  def sessionId: String
  // which room to connect
  def room: Option[String]
  // auth key from cookie/header/path/query param
  def token: Option[String]
  // player identified by auth string
  def player: Option[String]
  
}
