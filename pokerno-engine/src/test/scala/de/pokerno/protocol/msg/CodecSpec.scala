package de.pokerno.protocol.msg

import org.scalatest.FunSpec
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import de.pokerno.model
import de.pokerno.poker
import de.pokerno.protocol.Codec
import de.pokerno.protocol

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

    import Conversions._

    describe("Json") {
      /*
      it("AddBet") {
        val player = model.Player("A")
        val bet = model.Bet.fold
        val msg = AddBet(bet)
        val data = Codec.Json.encode(msg)
        val recover = Codec.Json.decode[Message](data)
        val e = recover.asInstanceOf[AddBet]
        e.bet should equal(msg.bet)
      }
      
      it("ShowCards") {
        val player = model.Player("A")
        val cards = poker.Cards("AhKhQd3c4s")
        val msg = ShowCards(cards)
        val data = Codec.Json.encode(msg)
        //Console printf("[ShowCards] data=%s", new String(data))
        val recover = Codec.Json.decode[Message](data)
        //Console printf("[ShowCards] recover=%s", recover)
        val e = recover.asInstanceOf[ShowCards]
        e.getCards should equal(msg.getCards)
      }
      
      it("ButtonChange") {
        val msg = ButtonChange(6)
        val data = Codec.Json.encode(msg)
        val recover = Codec.Json.decode[Message](data)
        recover.asInstanceOf[ButtonChange].button should equal(msg.button)
      }*/

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
