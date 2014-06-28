package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

sealed case class Mix(
    var variation: String,
    @Column(optionType = classOf[Int]) var speed: Option[Int],
    @Column("table_size") var tableSize: Int) extends KeyedEntity[Long] {
  var id: Long = 0
  def this() = this("", None, 0)
}
