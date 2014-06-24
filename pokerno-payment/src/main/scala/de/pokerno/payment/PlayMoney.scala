package de.pokerno.payment

object PlayMoney {
  trait RefillStrategy {
    import model._
    import java.time.temporal.{ChronoUnit, TemporalUnit}
    import java.time.Instant
    import concurrent.duration._
        
    final val refillAmount = 10000
    final val refillEvery = 1.hour
    
    def refill(balance: Balance, amount: Double = refillAmount) = {
      balance.getRecentBonus() match {
        case Some(bonus) =>
          val date = bonus.created
          val now = Instant.now()
          val deadline = now.minus(refillEvery.toHours, ChronoUnit.HOURS)
          if (!date.toInstant().isBefore(deadline)) {
            val diff = ChronoUnit.MINUTES.between(date.toInstant(), now)
            throw new thrift.Error("player %s: can't refill balance; last refill was %d minutes ago" format(balance.playerId, diff))
          }
        case _ =>
      }
      val bonus = Bonus.create(balance, amount - balance.amount)
      balance.charge(bonus.amount)
    }
    
  }
}
