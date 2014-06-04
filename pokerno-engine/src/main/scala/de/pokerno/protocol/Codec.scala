package de.pokerno.protocol

import com.fasterxml.jackson.databind.ObjectMapper

object Codec {

  trait Json {
    import de.pokerno.poker.Cards

    import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
  
    val mapper = new ObjectMapper with ScalaObjectMapper

    mapper.registerModule(DefaultScalaModule)
  }

}
