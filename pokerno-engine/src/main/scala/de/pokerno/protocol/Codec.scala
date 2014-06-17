package de.pokerno.protocol

import com.fasterxml.jackson.databind.ObjectMapper

object Codec {

  trait Json {
    import de.pokerno.poker.Cards

    import com.fasterxml.jackson.module.scala.DefaultScalaModule
    //import com.fasterxml.jackson.module.scala.
  
    val mapper = new ObjectMapper
//    val module = new TupleModule
//                    with OptionModule
//                    with MapModule
//                    with SeqModule
//                    with EnumerationModule
//                    with deser.ScalaStdValueInstantiatorsModule
//                    with introspect.ScalaClassIntrospectorModule

    mapper.registerModule(DefaultScalaModule)
    
    import com.fasterxml.jackson.annotation.JsonAutoDetect

    def encode[T](v: T) =
      mapper.writeValueAsString(v)

    def encodeAsBytes[T](v: T) =
      mapper.writeValueAsBytes(v)

    def encodeAsString[T](msg: T) =
      mapper.writeValueAsString(msg)
    
    def encodePretty[T](msg: T) =
      mapper.writerWithDefaultPrettyPrinter().writeValueAsString(msg)
      
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
