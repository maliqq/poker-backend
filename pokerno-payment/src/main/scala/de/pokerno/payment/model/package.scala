package de.pokerno.payment

package object model {
  def now() = java.sql.Timestamp.from(java.time.Instant.now())
}
