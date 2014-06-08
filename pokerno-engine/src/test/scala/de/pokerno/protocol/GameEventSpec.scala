package de.pokerno.protocol

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class GameEventSpec extends FunSpec with ClassicMatchers {
  import msg._
  import de.pokerno.model.{DealType, Bet, Street, Game, Stake}
  import de.pokerno.poker.{Cards, Hand}
  
  describe("GameEvent") {
    it("AskBet") {
      val e = AskBet(1, new Player("A"), 1000, (1000.0, 1000.0))
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"bet:ask","pos":1,"player":"A","call":1000,"raise":{"min":1000.0,"max":1000.0}}""")
    }
    
    it("AskDiscard") {
      val e = AskDiscard(1, new Player("A"))
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"discard:ask","pos":1,"player":"A"}""")
    }
    
    it("ButtonChange") {
      val e = ButtonChange(1)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"table:button","pos":1}""")
    }
    
    it("DealCards") {
      val e1 = DealHole(1, new Player("A"), Left(Cards.fromString("AsAd")))
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"cards:hole","pos":1,"player":"A","cards":"MTM="}""")
      
      val e2 = DealDoor(1, new Player("A"), Left(Cards.fromString("AsAd")))
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"cards:door","pos":1,"player":"A","cards":"MTM="}""")
      
      val e3 = DealBoard(Cards.fromString("AsAdAh"))
      val d3 = GameEvent.encodeAsString(e3)
      d3 should equal("""{"$type":"cards:board","cards":"MTMy"}""")
    }
    
    it("DeclareBet") {
      val e1 = DeclareBet(1, new Player("A"), Bet.fold)
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"bet:add","pos":1,"player":"A","fold":true}""")
      
      val e2 = DeclareBet(1, new Player("A"), Bet.check)
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"bet:add","pos":1,"player":"A","check":true}""")
      
//      val e3 = DeclareBet(1, new Player("A"), Bet.allIn)
//      val d3 = GameEvent.encodeAsString(e3)
//      d3 should equal("""{"$type":"bet:raise",}""")
      
      val e4 = DeclareBet(1, new Player("A"), Bet.call(1000))
      val d4 = GameEvent.encodeAsString(e4)
      d4 should equal("""{"$type":"bet:add","pos":1,"player":"A","call":1000}""")
      
      val e5 = DeclareBet(1, new Player("A"), Bet.raise(1000))
      val d5 = GameEvent.encodeAsString(e5)
      d5 should equal("""{"$type":"bet:add","pos":1,"player":"A","raise":1000}""")
      
      val e6 = DeclareBet(1, new Player("A"), Bet.ante(1000))
      val d6 = GameEvent.encodeAsString(e6)
      d6 should equal("""{"$type":"bet:add","pos":1,"player":"A","call":1000,"type":"ante"}""")
    }
    
    it("DeclareHand") {
      val e = DeclareHand(1, new Player("A"), Hand.High(Cards.fromString("AdAdKhKdJs")).get)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"hand:","pos":1,"player":"A","rank":"two-pair","cards":"MzMuLyU=","value":"MzMuLw==","high":"My4=","kicker":"JQ==","description":"two pairs, As and Ks"}""")
    }
    
//    it("DeclarePlayStart") {
//      val e = DeclarePlayStart()
//      val d = GameEvent.encodeAsString(e)
//      d should equal("""{"$type":"play:start"}""")
//    }
    
    it("DeclarePlayStop") {
      val e = DeclarePlayStop()
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"play:stop"}""")
    }
    
    it("DeclarePot") {
      val e1 = DeclarePot(1000)
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"pot:","pot":1000,"side":[]}""")
      
      val e2 = DeclarePot(1000, rake = Some(100))
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"pot:","pot":1000,"side":[],"rake":100}""")
    }
    
//    it("DeclareStart") {
//      val e = DeclareStart()
//      val d = GameEvent.encode(e)
//      d should equal("""{}""")
//    }
    
    it("DeclareStreet") {
      val e = DeclareStreet(Street.Preflop)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"street:","name":"preflop"}""")
    }
    
    it("DeclareWinner") {
      val e = DeclareWinner(1, new Player("A"), 1000)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"winner:","pos":1,"player":"A","amount":1000}""")
    }
    
    it("DiscardCards") {
      val e = DiscardCards(1, new Player("A"), cardsNum = Some(1))
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"cards:discard","pos":1,"player":"A","cardsNum":1}""")
    }
    
    it("GameChange") {
      val game = Game(Game.Texas, Game.NoLimit, 9)
      val e = GameChange(game)
      val d = GameEvent.encodeAsString(e)
      throw new Exception(d)
      d should equal("""{"$type":"game:","type":"texas","limit":"no-limit"}""")
    }
    
    it("JoinPlayer") {
      val e = PlayerJoin(1, new Player("A"), 1000.0)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"player:join","pos":1,"player":"A","amount":1000.0}""")
    }
    
    it("LeavePlayer") {
      val e = PlayerLeave(1, new Player("A"))
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"player:leave","pos":1,"player":"A"}""")
    }
//    
//    it("SeatEvent") {
//      val e = SeatEvent()
//      val d = GameEvent.encode(e)
//      d should equal("""{}""")
//    }
    
    it("ShowCards") {
      val e1 = ShowCards(1, new Player("A"), Cards.fromString("Ad"), muck = true)
      val d1 = GameEvent.encodeAsString(e1)
      d1 should equal("""{"$type":"cards:show","pos":1,"player":"A","cards":"Mw==","muck":true}""")
      
      val e2 = ShowCards(1, new Player("A"), Cards.fromString("Ad"), muck = false)
      val d2 = GameEvent.encodeAsString(e2)
      d2 should equal("""{"$type":"cards:show","pos":1,"player":"A","cards":"Mw==","muck":false}""")
    }
    
    it("StakeChange") {
      val e = StakeChange(100)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"stake:","bigBlind":100}""")
    }
    
    it("TickTimer") {
      val e = TickTimer(1, new Player("A"), 10)
      val d = GameEvent.encodeAsString(e)
      d should equal("""{"$type":"timer:tick","pos":1,"player":"A","timeLeft":10,"timeBank":false}""")
    }
  }
}
