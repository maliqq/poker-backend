package de.pokerno.playground

import de.pokerno.backend.gateway.{Http, http}
import de.pokerno.format.text
import akka.actor.{Actor, ActorSystem, Props}

object Main {
  val system = ActorSystem("poker-playground")
  
  def main(args: Array[String]) = {
    val htmlEventSource = system.actorOf(Props(classOf[Http.Gateway],
        http.Config(port = 8080, eventSource = Right(true))))
    
    val replayer = new Replayer(system)
    
    text.Parser.parse(scala.io.Source.fromFile(args(0))).foreach { tag =>
      Console printf("%s%s%s\n", Console.GREEN, tag, Console.RESET)
      replayer.process(tag)
    }
  }
}
