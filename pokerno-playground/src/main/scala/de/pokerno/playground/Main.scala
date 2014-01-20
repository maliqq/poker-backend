package de.pokerno.playground

import de.pokerno.backend.gateway.{Http, http}
import de.pokerno.format.text
import akka.actor.{Actor, ActorSystem, Props}

import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._

import jline.console.ConsoleReader

object Main {
  val system = ActorSystem("poker-playground")
  val consoleReader = new ConsoleReader
  consoleReader.setExpandEvents(false)
  
  def main(args: Array[String]) = {
    val htmlEventSource = system.actorOf(Props(classOf[Http.Gateway],
        http.Config(port = 8080, eventSource = Right(true))))
    
    Console printf("waiting for eventsource server to startup...\n")
    
    implicit val timeout = Timeout(5 seconds)
    val f = ask(htmlEventSource, "test")
    
    val listener = system.actorOf(Props(classOf[Listener], htmlEventSource))
    val replayer = new Replayer(listener)
    
    while (true) {
      val filename = consoleReader.readLine("Enter path to scenario >>> ")
      
      try {
        val src = scala.io.Source.fromFile(filename)
        text.Parser.parse(src).foreach { tag =>
          Console printf("%s%s%s\n", Console.GREEN, tag, Console.RESET)
          replayer.process(tag)
        }
      } catch {
        case e: Throwable =>
          Console printf("error: %s\n", e.getMessage)
          Console printf("%s", Console.CYAN)
          e.printStackTrace
          Console printf("%s\n", Console.RESET)
      }
    }
  }
}
