package de.pokerno.protocol

import org.scalatest._
import org.scalatest.Matchers._
import math.{BigDecimal => Decimal}

class CodecSpec extends FunSpec {

  import de.pokerno.model._
  import de.pokerno.model.seat.impl._
  import de.pokerno.poker.Deck
  
  object Json extends Codec.Json {
  }
  
  describe("Json.decode") {
    it("Game") {
      val game = Json.decodeFromString[Game]("""{"game":{"type":"texas","limit":"fixed-limit","tableSize":2}}""")
      game.`type` should equal(GameType.Texas)
      game.limit should equal(GameLimit.Fixed)
      game.tableSize should equal(2)
    }
    
    it("Mix") {
      val mix = Json.decodeFromString[Mix]("""{"mix":{"type":"horse","tableSize":2}}""")
      mix.`type` should equal(MixType.Horse)
      mix.tableSize should equal(2)
    }
    
    it("Stake") {
      val stake1 = Json.decodeFromString[Stake]("""{"bigBlind":100}""")
      stake1.bigBlind should equal(100)
      
      val stake2 = Json.decodeFromString[Stake]("""{"bigBlind":50,"smallBlind":20}""")
      stake2.smallBlind should equal(20)
      
      val stake3 = Json.decodeFromString[Stake]("""{"bigBlind":100,"ante":10}""")
      stake3.ante.get should equal(10)
      
      val stake4 = Json.decodeFromString[Stake]("""{"bigBlind":100,"bringIn":10}""")
      stake4.bringIn.get should equal(10)
      
      val stake5 = Json.decodeFromString[Stake]("""{"bigBlind":100,"smallBlind":null,"ante":null,"bringIn":null}""")
      stake5.smallBlind should equal(50.0)
      stake5.ante should be(None)
      stake5.bringIn should be(None)
    }
    
    it("Bet") {
      val call = Json.decodeFromString[Bet]("""{"call":10}""")
      call.isInstanceOf[Bet.Call] should be(true)
      
      val raise = Json.decodeFromString[Bet]("""{"raise":1000}""")
      raise.isInstanceOf[Bet.Raise] should be(true)
      
      val check = Json.decodeFromString[Bet]("""{"check":true}""")
      check should equal(Bet.Check)
      
      val fold = Json.decodeFromString[Bet]("""{"fold":true}""")
      fold should equal(Bet.Fold)
    }
  }
  
  describe("Json.encode") {
    it("Cards") {
      val cards = de.pokerno.poker.Cards.fromString("7dTd")
      Json.encode(cards: Array[Byte]) should equal(""""AQ="""")
    }
    
    it("2s") {
      val cards = de.pokerno.poker.Cards.fromString("2s")
      Json.encode(cards: Array[Byte]) should equal(""""FyM="""")
    }
    
    it("Stake") {
      val stake1 = Stake(100.0)
      Json.encode(stake1) should equal("""{"bigBlind":100.0,"smallBlind":50.00}""") // FIXME????
      
      val stake2 = Stake(0.5, Some(0.2))
      Json.encode(stake2) should equal("""{"bigBlind":0.5,"smallBlind":0.2}""")
      
      val stake3 = Stake(100.0, Some(50.0), ante = Left(10.0))
      Json.encode(stake3) should equal("""{"bigBlind":100.0,"smallBlind":50.0,"ante":10.0}""")
      
      // FIXME
      import math.{BigDecimal => Decimal}
      val stake4 = Stake(100.0: Decimal, Some(50.0: Decimal), ante = Left(10.0: Decimal), bringIn = Left(20.0: Decimal))
      Json.encode(stake4) should equal("""{"bigBlind":100.0,"smallBlind":50.0,"ante":10.0,"bringIn":20.0}""")
    }
    
    it("Game") {
      
      val game1 = Game(GameType.Texas, GameLimit.None)
      Json.encode(game1) should equal("""{"game":{"type":"texas","limit":"no-limit","tableSize":10}}""")
      
      val game2 = Mix(MixType.Horse)
      Json.encode(game2) should equal("""{"mix":{"type":"horse","tableSize":8}}""")
    }
    
    it("Variation") {
      val game: Variation = Game(GameType.Texas, GameLimit.None)
      Json.encode(game) should equal("""{"game":{"type":"texas","limit":"no-limit","tableSize":10}}""")
      
      val mix: Variation = Mix(MixType.Horse)
      Json.encode(mix) should equal("""{"mix":{"type":"horse","tableSize":8}}""")
    }
    
    it("Table") {
      val table = new Table(1)
      Json.encode(table) should equal("""{"button":0,"seats":[{"state":"empty"}]}""")
    }
    
    it("Position") {
      val seat = new Position(1, new Player("A"))
      Json.encode(seat) should equal("""{"pos":1,"player":"A"}""")
    }
    
    it("Acting") {
      val seat = new Acting(1, new Player("A"))
      Json.encode(seat) should equal("""{"pos":1,"player":"A"}""")
    }
    
    it("Sitting") {
      val seat1 = new Sitting(1, new Player("A"))
      Json.encode(seat1) should equal("""{"state":"taken","player":"A"}""")
      
      val seat2 = new Sitting(1, new Player("A"))
      seat2.offline()
      Json.encode(seat2) should equal("""{"state":"taken","player":"A","offline":true}""")
      
      val seat3 = new Sitting(1, new Player("A"))
      seat3.fold()
      Json.encode(seat3) should equal("""{"state":"fold","player":"A","action":{"fold":true}}""")
      
      val seat4 = new Sitting(1, new Player("A"))
      seat4.buyIn(10000)
      seat4.raise(100)
      Json.encode(seat4) should equal("""{"state":"bet","player":"A","stack":9900.0,"put":100.0,"action":{"raise":100}}""")
      
      val seat5 = new Sitting(1, new Player("A"))
      seat5.buyIn(10000)
      seat5.idle()
      Json.encode(seat5) should equal("""{"state":"idle","player":"A","stack":10000.0}""")
    }
    
    it("Pot") {
      val pot1 = new Pot
      Json.encode(pot1) should equal("""{"side":[],"total":0}""")
      
      val pot2 = new SidePot
      pot2.members("A") = 500
      pot2.members("B") = 1000
      
      Json.encode(pot2) should equal("""1500""")
    }
    
    it("Bet") {
      val bet1 = Bet.check
      Json.encode(bet1) should equal("""{"check":true}""")
      
      val bet2 = Bet.fold
      Json.encode(bet2) should equal("""{"fold":true}""")
      
      val bet3 = Bet.call(100)
      Json.encode(bet3) should equal("""{"call":100}""")
      
      val bet4 = Bet.raise(100)
      Json.encode(bet4) should equal("""{"raise":100}""")
      
      val bet5 = Bet.sb(100)
      Json.encode(bet5) should equal("""{"type":"small-blind","call":100}""")
      
      val bet6 = Bet.ante(100)
      Json.encode(bet6) should equal("""{"type":"ante","call":100}""")
    }
    
    import de.pokerno.gameplay
    
    it("betting.Round") {
      val table = new Table(1)
      val game = Game(GameType.Texas)
      val stake = Stake(100)
      val play = new Play
      val round = new gameplay.betting.Round(table, game, stake, play)
      val seat = new Sitting(1, new Player("A"))
      round.requireBet(seat)
      Json.encode(round) should equal("""{"pot":{"side":[],"total":0}}""")
    }
    
    it("PlayState") {
      val table = new Table(1)
      val game = Game(GameType.Texas)
      val stake = Stake(100)
      val deck = new Deck
      val dealer = new Dealer(deck)
      dealer.dealBoard(3)
      val events = new gameplay.Events("test")
      val play = new Play(java.util.UUID.randomUUID())
      val ctx = new gameplay.Context("test", table, game, stake, null, events, dealer = dealer, play = play)
      Json.encode(msg.PlayState(ctx)) should equal("""{}""")
    }
  }
  
}
