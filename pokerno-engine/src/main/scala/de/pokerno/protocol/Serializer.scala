package de.pokerno.protocol

//import com.fasterxml.jackson.core.{ JsonParser, JsonGenerator }
//import com.fasterxml.jackson.databind.{ SerializerProvider, DeserializationContext }
//import com.fasterxml.jackson.databind.ser.std.StdSerializer
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer

object Serializers {
  import de.pokerno.poker.Cards
  
  class Cards2Binary extends com.fasterxml.jackson.databind.util.StdConverter[Cards, Array[Byte]] {
    override def convert(c: Cards): Array[Byte] = c
  }
  
}
