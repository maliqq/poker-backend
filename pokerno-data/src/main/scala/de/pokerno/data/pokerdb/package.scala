package de.pokerno.data

package object pokerdb {
  def now() = java.sql.Timestamp.from(java.time.Instant.now())
}
