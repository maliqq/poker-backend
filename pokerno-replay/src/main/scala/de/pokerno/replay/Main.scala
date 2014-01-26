package de.pokerno.replay

import de.pokerno.format.text

import jline.console.ConsoleReader
import akka.actor.Actor

object Main {
  def main(args: Array[String]) = {
    if (args.length > 0) {
      val filename = args(0)
      if (filename != "") parse(filename)
    } else {
      val consoleReader = new ConsoleReader
      consoleReader.setExpandEvents(false)
      while (true) {
        val filename = consoleReader.readLine("Enter path to scenario >>> ")
        if (filename == "exit") {
          System.exit(0)
        }
        if (filename != "") parse(filename)
      }
    }
  }
  
  def parse(filename: String) {
    try {
      val src = scala.io.Source.fromFile(filename)
      val scenario = new Scenario()

      text.Parser.parse(src).foreach { case (line, lineno, tag) =>
        //Console printf("%s%s%s\n", Console.GREEN, tag, Console.RESET)
        scenario.process(tag)
      }

      Replayer.start(scenario)

    } catch {
      case _: java.io.FileNotFoundException =>
        Console printf("[ERROR] file not found\n")

      case err: ReplayError =>
        Console printf("[ERROR] %s%s%s\n", Console.RED, err.getMessage, Console.RESET)
      
      case err: Exception =>
        Console print("[ERROR]: ")
        Console printf("%s", Console.RED)
        err.printStackTrace
        Console printf("%s\n", Console.RESET)
    }
  }
  
}
