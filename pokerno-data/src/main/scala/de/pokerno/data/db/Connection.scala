package de.pokerno.data.db

import org.squeryl.adapters.PostgreSqlAdapter

object Connection {
  import org.squeryl.Session

  type Connector = ()=>Session
  
  def connector(): Connector = {
    connector(System.getProperties())
  }

  def connector(props: java.util.Properties): Connector = {
    val driver            = props.getProperty("database.driver")
    val url               = props.getProperty("database.url")
    val user              = props.getProperty("database.username")
    val password          = props.getProperty("database.password")
    val isDebug           = java.lang.Boolean.parseBoolean(props.getProperty("debug"))
    
    connector(driver, url, user, password, isDebug)
  }

  def connect(props: java.util.Properties = System.getProperties()) = {
    connector(props).apply()
  }
  
  def connector(driver: String, url: String, user: String, password: String, isDebug: Boolean = false): Connector = {
    val sessionCreator = () => {
      Class.forName(driver)
      val jdbcConnection = java.sql.DriverManager.getConnection(url, user, password)
      //jdbcConnection.setAutoCommit(false)
      val session = Session.create(jdbcConnection, new PostgreSqlAdapter {
        override def createSequenceName(fmd: org.squeryl.internals.FieldMetaData) = {
          fmd.parentMetaData.viewOrTable.name + "_" + fmd.columnName + "_seq"
        }
      })
      if (isDebug) {
        session.setLogger(println(_))
      }
      session
    }
    //SessionFactory.concreteFactory = Some(sessionCreator)
    sessionCreator
  }
}
