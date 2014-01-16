package de.pokerno.protocol

import collection.mutable.{ Map => MMap }
import org.msgpack.annotation.{ Message => MsgPack }

abstract class Codec {
  def encode[T <: Message](msg: T): Array[Byte]
  def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T
}

object Codec {
  object MsgPack extends Codec {
    import org.msgpack.ScalaMessagePack._

    val msgpack = org.msgpack.ScalaMessagePack.messagePack
    msgpack.register(classOf[msg.TableEventSchema.EventType])
    msgpack.register(classOf[msg.TableEventSchema.TableState])
    
    def encode[T <: Message](msg: T) =
      pack(msg)
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]) =
      unpack[T](data)
  }
  
  object Json extends Codec {
    import com.dyuproject.protostuff.ByteString
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
    
    def encode[T <: Message](msg: T) =
      mapper.writeValueAsBytes(msg)
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]) =
      mapper.readValue(data, manifest.erasure).asInstanceOf[T]
  }
  
  object Protobuf extends Codec {
    import com.dyuproject.protostuff.{ProtobufIOUtil => IOUtil, LinkedBuffer}
    
    def encode[T <: Message](msg: T) = {
      val buf = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE)
      IOUtil.toByteArray(msg, msg.schema, buf)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]) = {
      val instance: T = manifest.erasure.newInstance.asInstanceOf[T]
      IOUtil.mergeFrom(data, instance, instance.schema)
      instance
    }
  }
}
