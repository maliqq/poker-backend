package de.pokerno.backend.server.node.init

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object Database {
  type Storage = de.pokerno.backend.storage.PostgreSQL.Storage
  
  class ConnectionContext(sessionConnector: (() => org.squeryl.Session)) extends Actor with ActorLogging {
    val session = sessionConnector.apply()
    
    // FIXME
    org.squeryl.SessionFactory.externalTransactionManagementAdapter = Some(() => {
      Some(session)
    })

    def receive = { case _ => }
  }
}

trait Database {
  import Database._
  
  def buildStorage = new Storage
  
  def buildConnectionContext(sessionCreator: (() => org.squeryl.Session))(implicit system: ActorSystem) {
    system.actorOf(Props(classOf[ConnectionContext], sessionCreator))
  }
  
  def buildDatabaseService = new de.pokerno.data.pokerdb.Service

}
