package de.pokerno.protocol

import math.{ BigDecimal ⇒ Decimal }

trait ProtobufMessage {
  import com.dyuproject.protostuff.{ Schema, ByteString }
  implicit def schemaConv(s: Schema[_ <: Any]): Schema[Any] = s.asInstanceOf[Schema[Any]]

  implicit def byteArray2ByteString(v: Array[Byte]) =
    ByteString.copyFrom(v)

  def schema: Schema[Any]
  //def pipeSchema: protostuff.Pipe.Schema[_]
}

abstract class Message extends ProtobufMessage
