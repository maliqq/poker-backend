package de.pokerno.backend.protocol

import collection.mutable.{ Map => MMap }
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
    msgpack.register(classOf[TableEventSchema.TableState])
    
    def encode[T <: Message](msg: T): Array[Byte] =
      pack(msg)
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T =
      unpack[T](data)
  }
  
  object Json extends Codec {
    import protostuff.ByteString
    import com.fasterxml.jackson.databind.ObjectMapper
    import com.fasterxml.jackson.databind.module.SimpleModule
    
    val mapper = {
      val o = new ObjectMapper
      val m = new SimpleModule
      m.addSerializer(new ByteStringSerializer)
      m.addDeserializer(classOf[ByteString], new ByteStringDeserializer)
      o.registerModule(m)
      o
    }
    
    def encode[T <: Message](msg: T): Array[Byte] =
      mapper.writeValueAsBytes(msg)
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T =
      mapper.readValue(data, manifest.erasure).asInstanceOf[T]
    
    def decodeMessage(data: Array[Byte]): Message = mapper.readValue(data, classOf[Message])
  }
  
  object Protobuf extends Codec {
    def encode[T <: Message](msg: T): Array[Byte] = {
      val buf = protostuff.LinkedBuffer.allocate(protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
      protostuff.ProtostuffIOUtil.toByteArray(msg, msg.schema, buf)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val instance: T = manifest.erasure.newInstance.asInstanceOf[T]
      protostuff.ProtobufIOUtil.mergeFrom(data, instance, instance.schema)
      instance
    }
  }
}
