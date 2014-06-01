package de.pokerno.protocol

import com.fasterxml.jackson.databind.ObjectMapper

object Codec {

  trait Json {
    import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
  
    lazy val mapper = new ObjectMapper with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
  }

}
