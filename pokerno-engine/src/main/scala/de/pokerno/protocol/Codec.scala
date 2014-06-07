package de.pokerno.protocol

import com.fasterxml.jackson.databind.ObjectMapper

object Codec {

  trait Json {
    import de.pokerno.poker.Cards

    import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
  
    val mapper = new ObjectMapper with ScalaObjectMapper

    mapper.registerModule(DefaultScalaModule)

    def encode[T](v: T) =
      mapper.writeValueAsString(v)


    def encodeAsBytes[T](v: T) =
      mapper.writeValueAsBytes(v)

    def encodeAsString[T](msg: T) =
      mapper.writeValueAsString(msg)
      
    def decode[T <: Message](data: Array[Byte])(implicit manifest: Manifest[T]): T =
      mapper.readValue(data, manifest.runtimeClass).asInstanceOf[T]
    
    def decodeFromString[T](data: String)(implicit manifest: Manifest[T]): T =
      mapper.readValue(data, manifest.runtimeClass).asInstanceOf[T]

    def decodeValuesFromStream[T](f: java.io.InputStream)(implicit manifest: Manifest[T]) = {
      val t = mapper.getTypeFactory().constructCollectionType(classOf[java.util.ArrayList[_]], manifest.runtimeClass)
      mapper.readValue(f, t).asInstanceOf[java.util.ArrayList[T]]
    }

  }
  
  object Json extends Json {
  }

}
