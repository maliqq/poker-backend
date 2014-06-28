package de.pokerno.data.pokerdb.model

import org.squeryl._
import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

sealed case class Stake(
    @Column("big_blind") var bigBlind: Double,
    @Column("small_blind") var smallBlind: Double,
    @Column(optionType = classOf[Double])  var ante: Option[Double],
    @Column("buy_in_min") var buyInMin: Int,
    @Column("buy_in_max") var buyInMax: Int,
    @Column(name = "currency_id", optionType=classOf[Long]) var currencyId: Option[Long]) extends KeyedEntity[Long] {
  var id: Long = 0
  def this() = this(0, 0, None, 0, 0, None)
}