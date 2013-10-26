package de.pokerno.backend.protocol

import org.scalatest.FunSpec

import org.msgpack.annotation.{ Message => MsgPack }

@MsgPack
class TestMsg {
  var i: Int = 0
}

class CodecSpec extends FunSpec {
  describe("Codec") {
    describe("MsgPack") {
      it("pack") {
        val msgpack = new org.msgpack.MessagePack
        msgpack.write(new TestMsg)
      }
    }
  }
}
