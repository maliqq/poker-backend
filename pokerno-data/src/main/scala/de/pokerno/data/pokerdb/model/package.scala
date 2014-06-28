package de.pokerno.data.pokerdb

package object model {
  type UUID =  java.util.UUID
  type Timestamp = java.sql.Timestamp

  def now() = java.sql.Timestamp.from(java.time.Instant.now())
}
