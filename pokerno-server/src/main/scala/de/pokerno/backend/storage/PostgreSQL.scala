package de.pokerno.backend.storage

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.internals.FieldMetaData

object PostgreSQL {
  object Connection {
    def connect(driver: String, url: String, user: String, password: String): Session = {
      val sessionCreator = () => {
        Class.forName(driver)
        val jdbcConnection = java.sql.DriverManager.getConnection(url, user, password)
        //jdbcConnection.setAutoCommit(false)
        Session.create(jdbcConnection, new PostgreSqlAdapter {
          import org.squeryl.internals.FieldMetaData
          override def createSequenceName(fmd: FieldMetaData) = fmd.parentMetaData.viewOrTable.name + "_" + fmd.columnName + "_seq"
        })
      }
      //SessionFactory.concreteFactory = Some(sessionCreator)
      sessionCreator()
    }
  }
}
