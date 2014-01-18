package de.pokerno.playground

import de.pokerno.backend.gateway.{Http, http}
import de.pokerno.format.text
import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Await
import concurrent.duration._

object Main {
  val system = ActorSystem("poker-playground")
  
  implicit val timeout = Timeout(5 seconds)
  
  class Test extends Actor {
    def receive = {
      case m: Any =>
        Console printf("got: %s\n\n", m)
    }
  }
  
  def main(args: Array[String]) = {
    //val htmlEventSource = system.actorOf(Props(classOf[Http.Gateway],
    //    http.Config(port = 8080, eventSource = Right(true))))
    
    val a = system.actorOf(Props(classOf[Test]))
    val b = system.actorOf(Props(classOf[Replayer], a))
    
    text.Parser.parse(scala.io.Source.fromFile("deal.txt")).foreach { tag =>
      Console printf("%s%s%s\n", Console.GREEN, tag, Console.RESET)
      b ! tag
    }
  }
}
