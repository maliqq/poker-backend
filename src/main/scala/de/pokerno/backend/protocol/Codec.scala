package de.pokerno.backend.protocol

import scala.collection.mutable.{ Map => MMap }
import org.msgpack.annotation.{ Message => MsgPack }
import org.msgpack.ScalaMessagePack
import com.dyuproject.protostuff

abstract class Codec {
  def encode[T <: Message](msg: T): Array[Byte]
  def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T
}

object Codec {
  object MsgPack extends Codec {
    import org.msgpack.ScalaMessagePack._

    val msgpack = org.msgpack.ScalaMessagePack.messagePack
    msgpack.register(classOf[TableEventSchema.EventType])
    
    def encode[T <: Message](msg: T): Array[Byte] = {
      pack(msg)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      unpack[T](data)
    }
  }
  
  object Json extends Codec {
    def encode[T <: Message](msg: T): Array[Byte] = {
      val schema: protostuff.Schema[T] = msg.schema.asInstanceOf[protostuff.Schema[T]]
      val buf = protostuff.LinkedBuffer.allocate(protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
      protostuff.JsonIOUtil.toByteArray(msg, schema, false)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val instance: T = manifest.erasure.newInstance.asInstanceOf[T]
      val schema: protostuff.Schema[T] = instance.schema.asInstanceOf[protostuff.Schema[T]]
      protostuff.JsonIOUtil.mergeFrom(data, instance, schema, false)
      instance
    }
  }
  
  object Protobuf extends Codec {
    def encode[T <: Message](msg: T): Array[Byte] = {
      val schema: protostuff.Schema[T] = msg.schema.asInstanceOf[protostuff.Schema[T]]
      val buf = protostuff.LinkedBuffer.allocate(protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
      protostuff.ProtostuffIOUtil.toByteArray(msg, schema, buf)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val instance: T = manifest.erasure.newInstance.asInstanceOf[T]
      val schema: protostuff.Schema[T] = instance.schema.asInstanceOf[protostuff.Schema[T]]
      protostuff.ProtobufIOUtil.mergeFrom(data, instance, schema)
      instance
    }
  }
}
