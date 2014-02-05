package de.pokerno.util

object ConsoleUtils {
  
  private val useColor = (System.getProperty("color") != null)
  
  private def colorify(color: String, format: String, args: Any*) {
    Console printf(color + format + "\n" + Console.RESET, args:_*)
  }
  
  private def tagify(tag: String, format: String, args: Any*) {
    Console printf("[" + tag + "] " + format + "\n", args:_*)
  }
  
  def fatal(err: Throwable) {
    val msg = err.getMessage + "\n" + err.getStackTraceString
    if (useColor) colorify(Console.RED, msg)
    else tagify("ERROR", msg)
  }
  
  def error(subject: Any, args: Any*) {
    subject match {
      case err: Throwable =>
        val msg = err.getMessage
        if (useColor) colorify(Console.RED, msg)
        else tagify("ERROR", msg)
      
      case format: String =>
        if (useColor) colorify(Console.RED, format, args:_*)
        else tagify("ERROR", format, args:_*)
    }
  }
  
  def info(format: String, args: Any*) {
    if (useColor) colorify(Console.GREEN, format, args:_*)
    else tagify("INFO", format, args:_*)
  }
  
  def warn(format: String, args: Any*) {
    if (useColor) colorify(Console.YELLOW, format, args:_*)
    else tagify("WARN", format, args:_*)
  }
  
  def debug(format: String, args: Any*) {
    if (useColor) colorify(Console.CYAN, format, args:_*)
    else tagify("DEBUG", format, args:_*)
  }
  
}
