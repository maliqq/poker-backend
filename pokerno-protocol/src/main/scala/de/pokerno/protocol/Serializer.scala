package de.pokerno.protocol

import com.dyuproject.protostuff.ByteString
import com.fasterxml.jackson.core.{ JsonParser, JsonGenerator }
import com.fasterxml.jackson.databind.{ SerializerProvider, DeserializationContext }
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class ByteStringSerializer extends StdSerializer[ByteString](classOf[ByteString]) {
  override def serialize(o: ByteString, g: JsonGenerator, p: SerializerProvider) {
    g.writeObject(o.toByteArray)
  }
}

class ByteStringDeserializer extends StdDeserializer[ByteString](classOf[ByteString]) {
  override def deserialize(p: JsonParser, ctx: DeserializationContext): ByteString =
    ByteString.copyFrom(p.getBinaryValue)
}
