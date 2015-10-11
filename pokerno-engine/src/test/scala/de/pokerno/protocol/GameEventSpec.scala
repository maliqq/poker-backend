package de.pokerno.protocol

import org.scalatest._
import org.scalatest.Matchers._

class GameEventSpec extends FunSpec {
  import msg._
  import de.pokerno.model._
  import de.pokerno.model.seat.impl._
  import de.pokerno.poker._
  import de.pokerno.gameplay
  
  describe("GameEvent") {
    it("AskBet") {
      val seat = new Sitting(1, new Player("A"))
      
      seat.call = 1000
      seat.raise = (1000, 1000)

      val e = AskBet(seat)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"bet:ask","pos":1,"player":"A","call":1000,"raise":[1000,1000]}""")
    }
    
    it("AskDiscard") {
      val seat = new Sitting(1, new Player("A"))
      
      val e = AskDiscard(seat)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"discard:ask","pos":1,"player":"A"}""")
    }
    
    it("ButtonChange") {
      val e = ButtonChange(1)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"table:button","pos":1}""")
    }
    
    it("DealCards") {
      val seat = new Sitting(1, new Player("A"))
      val e1 = DealHole(seat, Left(Cards.fromString("AsAd")))
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"cards:hole","pos":1,"player":"A","cards":"MTM="}""")
      
      val e2 = DealDoor(seat, Left(Cards.fromString("AsAd")))
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"cards:door","pos":1,"player":"A","cards":"MTM="}""")
      
      val e3 = DealBoard(Cards.fromString("AsAdAh"))
      val d3 = GameEvent.encodeAsString(e3)
      d3 should equal("""{"$type":"cards:board","cards":"MTMy"}""")
    }
    
    it("DeclareBet") {
      val seat = new Sitting(1, new Player("A"))
      
      val e1 = DeclareBet(seat, Bet.fold)
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"bet:add","pos":1,"player":"A","action":{"fold":true}}""")
      
      val e2 = DeclareBet(seat, Bet.check)
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"bet:add","pos":1,"player":"A","action":{"check":true}}""")
      
//      val e3 = DeclareBet(seat, Bet.allIn)
//      val d3 = GameEvent.encodeAsString(e3)
//      d3 should equal("""{"$type":"bet:raise",}""")
      
      val e4 = DeclareBet(seat, Bet.call(1000))
      val d4 = GameEvent.encodeAsString(e4)
      d4 should equal("""{"$type":"bet:add","pos":1,"player":"A","action":{"call":1000}}""")
      
      val e5 = DeclareBet(seat, Bet.raise(1000))
      val d5 = GameEvent.encodeAsString(e5)
      d5 should equal("""{"$type":"bet:add","pos":1,"player":"A","action":{"raise":1000}}""")
      
      val e6 = DeclareBet(seat, Bet.ante(1000))
      val d6 = GameEvent.encodeAsString(e6)
      d6 should equal("""{"$type":"bet:add","pos":1,"player":"A","action":{"type":"ante","call":1000}}""")
    }
    
    it("DeclareHand") {
      val seat = new Sitting(1, new Player("A"))
      
      val e = DeclareHand(seat, Cards.fromString("KdJs"), Hand.High(Cards.fromString("AdAdKhKdJs")).get)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"hand:","pos":1,"player":"A","cards":"LyU=","hand":{"rank":"two-pair","cards":"MzMuLyU=","value":"MzMuLw==","high":"My4=","kicker":"JQ==","description":"two pairs, As and Ks"}}""")
    }
    
    def gameplayContext: gameplay.Context = {
      val table = new Table(1)
      val game = Game(GameType.Texas)
      val stake = Stake(100)
      val deck = new Deck
      val dealer = new Dealer(deck)
      dealer.dealBoard(3)
      val exchange = new de.pokerno.hub.impl.Topic[gameplay.Notification]("test")
      val events = new gameplay.Publisher("test", exchange)
      val play = new Play(java.util.UUID.randomUUID())
      val ctx = new gameplay.Context("test", table, game, stake, null, events, dealer = dealer, play = play)
      ctx
    }
    
    it("DeclarePlayStart") {
      val e = DeclarePlayStart(gameplayContext)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"play:start"}""")
    }
    
    it("DeclarePlayStop") {
      val e = DeclarePlayStop()
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"play:stop"}""")
    }
    
    it("DeclarePot") {
      val pot = new Pot
      pot.main.add(new Player("A"), 1000)
      pot.complete()
      
      val e1 = DeclarePot(pot)
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"pot:","pot":{"total":1000,"side":[1000]}}""")
      
      val rake = new SidePot()
      rake.add(new Player("B"), 100)
      
      val e2 = DeclarePot(pot, Some(rake))
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"pot:","pot":{"total":1000,"side":[1000]},"rake":100}""")
    }
    
    it("DeclareStart") {
      val e = DeclareStart(gameplayContext, "")
      val d = GameEvent.encodeAsString(e)
      d should equal("""{}""")
    }
    
    it("DeclareStreet") {
      val e = DeclareStreet(Street.Preflop)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"street:","name":"preflop"}""")
    }
    
    it("DeclareWinner") {
      val seat = new Sitting(1, new Player("A"))
      
      val e = DeclareWinner(seat, 1000)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"winner:","pos":1,"player":"A","amount":1000}""")
    }
    
    it("DiscardCards") {
      val seat = new Sitting(1, new Player("A"))
      
      val e = DiscardCards(seat, cardsNum = Some(1))
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"cards:discard","pos":1,"player":"A","cardsNum":1}""")
    }
    
    it("GameChange") {
      val game = new Game(GameType.Texas, GameLimit.None, 9)
      val e = GameChange(game)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"game:","game":"texas","limit":"no-limit"}""")
    }
    
    it("JoinPlayer") {
      val seat = new Sitting(1, new Player("A"))
      
      val e = PlayerJoin(seat, 1000.0)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"player:join","pos":1,"player":"A","amount":1000.0}""")
    }
    
    it("LeavePlayer") {
      val seat = new Sitting(1, new Player("A"))
      
      val e = PlayerLeave(seat)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"player:leave","pos":1,"player":"A"}""")
    }
    
    it("ShowCards") {
      val seat = new Sitting(1, new Player("A"))
      
      val e1 = ShowCards(seat, Cards.fromString("Ad"), muck = true)
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"cards:show","pos":1,"player":"A","cards":"Mw==","muck":true}""")
      
      val e2 = ShowCards(seat, Cards.fromString("Ad"), muck = false)
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"cards:show","pos":1,"player":"A","cards":"Mw==","muck":false}""")
    }
    
    it("StakeChange") {
      val e = StakeChange(100)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"stake:","bigBlind":100}""")
    }
    
    it("TickTimer") {
      val seat = new Sitting(1, new Player("A"))
      
      val e = TickTimer(seat, 10)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"timer:tick","pos":1,"player":"A","timeLeft":10,"timeBank":false}""")
    }
  }
}
