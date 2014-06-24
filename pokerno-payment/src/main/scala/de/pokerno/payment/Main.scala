package de.pokerno.payment

import java.util.UUID

object Main {
  
  def main(args: Array[String]) {
//    val addr = new java.net.InetSocketAddress("127.0.0.1", 3004)
//    Console printf("starting at %s\n", addr)
//    Service.start(addr)
    
    val props = new java.util.Properties
    props.load(new java.io.FileInputStream("./etc/database.properties"))
    val session = de.pokerno.data.db.Connection.connect(props)
    //session.setLogger(println(_))
    session.bindToCurrentThread
    
    val currency = PaymentDB.getCurrencyByCode("USD")
    val playerId = UUID.fromString("255fd68b-e948-47d3-9948-2c42fc372d94")
    //val roomId = UUID.fromString("30552f31-f871-4d05-aa08-83402a01efba")
    val roomId = UUID.fromString("ab7bbca7-e70f-4dee-9049-285da745f41b") // USD
    val tournamentId = UUID.fromString("6f9123a3-ad75-48e6-bc81-19c12f29f96a")
    
    PaymentDB.createBalance(playerId, initial = 10000, currencyId = Some(currency.id))
    PaymentDB.join(playerId, 500, roomId)
  }
  
}
