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
  
  import protostuff.runtime.{ RuntimeSchema => rs }
  rs.register(classOf[Bet], BetSchema.SCHEMA)
  rs.register(classOf[Range], RangeSchema.SCHEMA)
  rs.register(classOf[Game], GameSchema.SCHEMA)
  rs.register(classOf[Hand], HandSchema.SCHEMA)
  rs.register(classOf[Seat], SeatSchema.SCHEMA)
  rs.register(classOf[Stake], StakeSchema.SCHEMA)
  rs.register(classOf[Table], TableSchema.SCHEMA)
  rs.register(classOf[RequireBet], RequireBetSchema.SCHEMA)
  rs.register(classOf[RequireDiscard], RequireDiscardSchema.SCHEMA)
  rs.register(classOf[DealCards], DealCardsSchema.SCHEMA)
  rs.register(classOf[DeclareHand], DeclareHandSchema.SCHEMA)
  rs.register(classOf[DeclarePot], DeclarePotSchema.SCHEMA)
  rs.register(classOf[DeclareWinner], DeclareWinnerSchema.SCHEMA)
  rs.register(classOf[Event], EventSchema.SCHEMA)
    rs.register(classOf[ActionEvent], ActionEventSchema.SCHEMA)
    rs.register(classOf[GameplayEvent], GameplayEventSchema.SCHEMA)
    rs.register(classOf[StageEvent], StageEventSchema.SCHEMA)
    rs.register(classOf[TableEvent], TableEventSchema.SCHEMA)
    rs.register(classOf[SeatEvent], SeatEventSchema.SCHEMA)
    rs.register(classOf[DealEvent], DealEventSchema.SCHEMA)
  rs.register(classOf[Msg], MsgSchema.SCHEMA)
  rs.register(classOf[Cmd], CmdSchema.SCHEMA)
    rs.register(classOf[JoinTable], JoinTableSchema.SCHEMA)

  object Json extends Codec {
    def encode[T <: Message](msg: T): Array[Byte] = {
      val schema: protostuff.Schema[T] = rs.getSchema(msg.getClass.asInstanceOf[Class[T]])
      val buf = protostuff.LinkedBuffer.allocate(protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
      protostuff.JsonIOUtil.toByteArray(msg, schema, false)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val schema: protostuff.Schema[T] = rs.getSchema(manifest.erasure.asInstanceOf[Class[T]])
      val v: T = schema.newMessage
      protostuff.JsonIOUtil.mergeFrom(data, v, schema, false)
      v
    }
  }
  
  object Protobuf extends Codec {
    def encode[T <: Message](msg: T): Array[Byte] = {
      val schema: protostuff.Schema[T] = rs.getSchema(msg.getClass.asInstanceOf[Class[T]])
      val buf = protostuff.LinkedBuffer.allocate(protostuff.LinkedBuffer.DEFAULT_BUFFER_SIZE)
      protostuff.ProtostuffIOUtil.toByteArray(msg, schema, buf)
    }
    
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T = {
      val schema: protostuff.Schema[T] = rs.getSchema(manifest.erasure.asInstanceOf[Class[T]])
      val v: T = schema.newMessage
      protostuff.ProtobufIOUtil.mergeFrom(data, v, schema)
      v
    }
  }
}
