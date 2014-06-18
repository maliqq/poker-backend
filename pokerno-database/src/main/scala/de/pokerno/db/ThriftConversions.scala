package de.pokerno.db

object ThriftConversions {
  
  import de.pokerno.protocol.{thrift => protocol}

  implicit def state2string(s: thrift.State): String = s.name.toLowerCase
  implicit def string2state(s: String): thrift.State = thrift.State.valueOf(s).get
  
  implicit def string2game(s: String): protocol.GameType = protocol.GameType.valueOf(s).get
  implicit def string2mix(s: String): protocol.MixType = protocol.MixType.valueOf(s).get
  implicit def string2limit(s: String): protocol.GameLimit = protocol.GameLimit.valueOf(s).get
  
  implicit def stake2thrift(s: Database.Stake): protocol.Stake = protocol.Stake(s.bigBlind, Option(s.smallBlind), s.ante, s.bringIn, s.buyInMin, s.buyInMax)
  
  implicit def game2thrift(s: Database.Game): protocol.Game = protocol.Game(s.variation, s.limit.get, s.tableSize, s.speed)
  implicit def mix2thrift(s: Database.Mix): protocol.Mix = protocol.Mix(s.variation, s.tableSize, s.speed)
  
}
