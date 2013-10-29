package de.pokerno.backend.protocol

import scala.collection.mutable.{ Map => MMap }
import org.msgpack.annotation.{ Message => MsgPack }
import org.msgpack.ScalaMessagePack
import org.omg.CORBA_2_3.portable.OutputStream

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
  
  class Json extends Codec {
    import com.dyuproject.protostuff
    
    def encode[T <: Message](msg: T) = {
      val schema: protostuff.Schema[T] = protostuff.runtime.RuntimeSchema.getSchema(msg.getClass.asInstanceOf[Class[T]])
      val buf = protostuff.LinkedBuffer.allocate(protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
      protostuff.JsonIOUtil.toByteArray(msg, schema, false)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val schema: protostuff.Schema[T] = protostuff.runtime.RuntimeSchema.getSchema(manifest.erasure.asInstanceOf[Class[T]])
      val v: T = schema.newMessage
      protostuff.JsonIOUtil.mergeFrom(data, v, schema, false)
      v
    }
  }
  
  object Protobuf extends Codec {
    import com.dyuproject.protostuff

    def encode[T <: Message](msg: T) = {
      val schema: protostuff.Schema[T] = protostuff.runtime.RuntimeSchema.getSchema(msg.getClass.asInstanceOf[Class[T]])
      val buf = protostuff.LinkedBuffer.allocate(protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
      protostuff.ProtostuffIOUtil.toByteArray(msg, schema, buf)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val schema: protostuff.Schema[T] = protostuff.runtime.RuntimeSchema.getSchema(manifest.erasure.asInstanceOf[Class[T]])
      val v: T = schema.newMessage
      protostuff.ProtobufIOUtil.mergeFrom(data, v, schema)
      v
    }
  }
}
