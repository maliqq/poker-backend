package de.pokerno.protocol.msg

import org.scalatest.FunSpec
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import de.pokerno.model
import de.pokerno.poker
import de.pokerno.protocol.Codec

class CodecSpec extends FunSpec with ClassicMatchers {
  describe("Codec") {
    
    describe("MsgPack") {
      it("ButtonChange") {
        val msg = ButtonChange(6)
        val data = Codec.MsgPack.encode(msg)
        val recover = Codec.MsgPack.decode[ButtonChange](data)
        recover.button should equal(msg.button)
      }
    }

    describe("Protobuf") {
      it("ButtonChange") {
        val msg = ButtonChange(6)
        val data = Codec.Protobuf.encode(msg)
        val recover = Codec.Protobuf.decode[ButtonChange](data)
        recover.button should equal(msg.button)
      }
    }
    
    
    describe("Json") {
      it("AddBet") {
        val player = model.Player("A")
        val bet = model.Bet.fold
        val msg = AddBet(1, player, bet)
        val data = Codec.Json.encode(msg)
        val recover = Codec.Json.decode[Message](data)
        val addBet = recover.asInstanceOf[AddBet]
        addBet.getPos should equal(msg.getPos)
        addBet.getPlayer should equal(msg.getPlayer)
        addBet._bet should equal(msg._bet)
      }
      
      it("ShowCards") {
        val player = model.Player("A")
        val cards = poker.Cards("AhKhQd3c4s")
        val msg = ShowCards(1, player, cards)
        val data = Codec.Json.encode(msg)
        //Console printf("[ShowCards] data=%s", new String(data))
        val recover = Codec.Json.decode[Message](data)
        //Console printf("[ShowCards] recover=%s", recover)
        val showCards = recover.asInstanceOf[ShowCards]
        showCards.getPos should equal(msg.getPos)
        showCards.getPlayer should equal(msg.getPlayer)
        showCards.getCards should equal(msg.getCards)
      }
      
      it("ButtonChange") {
        val msg = ButtonChange(6)
        val data = Codec.Json.encode(msg)
        val recover = Codec.Json.decode[Message](data)
        recover.asInstanceOf[ButtonChange].button should equal(msg.button)
      }
      
      it("GameChange") {}
      it("StakeChange") {}
      it("PlayStart") {}
      it("PlayStop") {}
      it("StreetStart") {}
      it("DealCards") {}
      it("RequireBet") {}
      it("RequireDiscard") {}
      it("DeclarePot") {}
      it("DeclareHand") {}
      it("DeclareWinner") {}
      it("JoinTable") {}
      it("Chat") {}
      it("Dealer") {}
      it("Error") {}
    }
  }
}
