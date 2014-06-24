package de.pokerno.data.db

import org.squeryl.adapters.PostgreSqlAdapter

object Connection {
  import org.squeryl.Session
  
  def connect(): Session = {
    val props = System.getProperties()
    connect(props)
  }
  
  def connect(props: java.util.Properties): Session = {
    val driver            = props.getProperty("database.driver")
    val url               = props.getProperty("database.url")
    val user              = props.getProperty("database.username")
    val password          = props.getProperty("database.password")
    
    connect(driver, url, user, password)
  }
  
  def connect(driver: String, url: String, user: String, password: String): Session = {
    val sessionCreator = () => {
      Class.forName(driver)
      val jdbcConnection = java.sql.DriverManager.getConnection(url, user, password)
      //jdbcConnection.setAutoCommit(false)
      Session.create(jdbcConnection, new PostgreSqlAdapter {
        override def createSequenceName(fmd: org.squeryl.internals.FieldMetaData) = {
          fmd.parentMetaData.viewOrTable.name + "_" + fmd.columnName + "_seq"
        }
      })
    }
    //SessionFactory.concreteFactory = Some(sessionCreator)
    sessionCreator()
  }
}
