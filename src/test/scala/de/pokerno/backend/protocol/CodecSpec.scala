package de.pokerno.backend.protocol

import org.scalatest.FunSpec
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import de.pokerno.model.{ Player, Bet }

class CodecSpec extends FunSpec with ClassicMatchers {
  describe("Codec") {
    describe("MsgPack") {
      it("pack") {
      }
    }

    describe("Protobuf") {
      import com.dyuproject.protostuff._
      it("pack") {
      }
    }
    
    describe("Json") {
      it("pack") {
      }
    }
  }
}
