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
    msgpack.register(classOf[TableEventSchema.TableState])
    
    def encode[T <: Message](msg: T): Array[Byte] = {
      pack(msg)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      unpack[T](data)
    }
    
    def decode(data: Array[Byte]): Message = null
  }
  
  object Json extends Codec {
    final val TypeField = "$type"
    final val ObjectField = "$object"
    
    import org.codehaus.jackson.JsonToken
    import org.codehaus.jackson.io.IOContext
    import org.codehaus.jackson.impl.Utf8StreamParser
    import protostuff.JsonIOUtil
    
    def encode[T <: Message](msg: T): Array[Byte] = {
      protostuff.JsonIOUtil.toByteArray(msg, msg.schema.asInstanceOf[protostuff.Schema[Any]], false)
    }
    
    def encodeExplicit[T <: Message](msg: T): Array[Byte] = {
      val schema: protostuff.Schema[T] = msg.schema.asInstanceOf[protostuff.Schema[T]]
      
      val out = new java.io.ByteArrayOutputStream
      val context = new IOContext(JsonIOUtil.DEFAULT_JSON_FACTORY._getBufferRecycler(), out, false);
      val gen = JsonIOUtil.newJsonGenerator(out, context.allocWriteEncodingBuffer)
      
      try {
        gen.writeStartObject
        gen.writeStringField(TypeField, msg.getClass.getSimpleName)
        gen.writeFieldName(ObjectField)
        protostuff.JsonIOUtil.writeTo(gen, msg, schema, false)
      } finally {
        gen.writeEndObject
        gen.close
      }
      
      out.toByteArray
    }
    
    final val registry = Map[String, Class[_ <: Message]](
        
        "AddBet" -> classOf[AddBet],
        "DiscardCards" -> classOf[DiscardCards],
        "ShowCards" -> classOf[ShowCards],
        "ButtonChange" -> classOf[ButtonChange],
        "GameChange" -> classOf[GameChange],
        "StakeChange" -> classOf[StakeChange],
        "PlayStart" -> classOf[PlayStart],
        "PlayStop" -> classOf[PlayStop],
        "StreetStart" -> classOf[StreetStart],
        "DealCards" -> classOf[DealCards],
        "RequireBet" -> classOf[RequireBet],
        "RequireDiscard" -> classOf[RequireDiscard],
        "DeclarePot" -> classOf[DeclarePot],
        "DeclareHand" -> classOf[DeclareHand],
        "DeclareWinner" -> classOf[DeclareWinner],
        "JoinTable" -> classOf[JoinTable],
        "Chat" -> classOf[Chat],
        "Dealer" -> classOf[Dealer],
        "Error" -> classOf[Error]
        
    )
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val instance: T = manifest.erasure.newInstance.asInstanceOf[T]
      val schema: protostuff.Schema[T] = instance.schema.asInstanceOf[protostuff.Schema[T]]
      protostuff.JsonIOUtil.mergeFrom(data, instance, schema, false)
      instance
    }
    
    def decodeExplicit(data: Array[Byte]): Message = {
      val context = new IOContext(JsonIOUtil.DEFAULT_JSON_FACTORY._getBufferRecycler, data, false)
      val parser = new Utf8StreamParser(context, 
                JsonIOUtil.DEFAULT_JSON_FACTORY.getParserFeatures(), null, 
                JsonIOUtil.DEFAULT_JSON_FACTORY.getCodec(), 
                JsonIOUtil.DEFAULT_JSON_FACTORY.getRootByteSymbols().makeChild(true, true), 
                data, 0, data.length, false)
      var _type: String = ""
      var _instance: Message = null

      if (parser.nextToken != JsonToken.START_OBJECT) {
        // TODO handle this
      }
      
      if (parser.nextToken != JsonToken.FIELD_NAME) {
        // TODO handle this
      }
      
      if (parser.getCurrentName == TypeField && parser.nextToken == JsonToken.VALUE_STRING) {
        _type = parser.getText
      }
      
      if (parser.nextToken != JsonToken.FIELD_NAME) {
        // TODO handle this
      }
      
      if (parser.getCurrentName == ObjectField)
        registry.get(_type) match {
          case Some(_class) =>
            _instance = _class.newInstance
            val schema = _instance.schema.asInstanceOf[protostuff.Schema[Any]]
            protostuff.JsonIOUtil.mergeFrom(parser, _instance, schema, false)
          case None =>
            // TODO handle this
        }
      
      if (parser.nextToken != JsonToken.END_OBJECT) {
        // TODO handle this
      }
      _instance
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
