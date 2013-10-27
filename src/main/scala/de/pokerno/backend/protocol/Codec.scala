package de.pokerno.backend.protocol

import scala.collection.mutable.{ Map => MMap }
import org.msgpack.annotation.{ Message => MsgPack }
import org.msgpack.ScalaMessagePack

abstract class Codec {
//  def encode[T <: Message](msg: T)(implicit manifest: Manifest[T]): Array[Byte]
//  def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T
}

object Codec {
  object MsgPack extends Codec {
    import org.msgpack.ScalaMessagePack._
  }
  
  object Json extends Codec {
  }
  
  class Protobuf extends Codec {
  }
}
