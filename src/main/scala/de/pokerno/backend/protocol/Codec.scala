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
    final val TypeField = "$type"
    final val ObjectField = "$object"
    
    def encode[T <: Message](msg: T): Array[Byte] = {
      val schema: protostuff.Schema[T] = msg.schema.asInstanceOf[protostuff.Schema[T]]
      
      val out = new java.io.ByteArrayOutputStream
      val context = new org.codehaus.jackson.io.IOContext(protostuff.JsonIOUtil.DEFAULT_JSON_FACTORY._getBufferRecycler(), out, false);
      val gen = protostuff.JsonIOUtil.newJsonGenerator(out, context.allocWriteEncodingBuffer)
      
      try {
        gen.writeStartObject
        gen.writeStringField(TypeField, schema.messageName)
        gen.writeFieldName(ObjectField)
        protostuff.JsonIOUtil.writeTo(gen, msg, schema, false)
      } finally {
        gen.writeEndObject
        gen.close
      }
      
      out.toByteArray
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
