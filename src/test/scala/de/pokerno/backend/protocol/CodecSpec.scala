package de.pokerno.backend.protocol

import org.scalatest.FunSpec
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import de.pokerno.model

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
      it("ButtonChange") {
        val msg = ButtonChange(6)
        val data = Codec.Json.encode(msg)
        val recover = Codec.Json.decode[ButtonChange](data)
        recover.button should equal(msg.button)
      }
    }
  }
}
