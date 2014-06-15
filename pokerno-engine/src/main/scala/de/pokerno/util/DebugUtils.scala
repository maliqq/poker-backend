package de.pokerno.util

object Colored {
  private val useColor = (System.getProperty("log.text") == null)

  private def colored(color: String, format: String, args: Any*) {
    Console printf (color + format + "\n" + Console.RESET, args: _*)
  }

  private def tagged(tag: String, format: String, args: Any*) {
    Console printf ("[" + tag + "] " + format + "\n", args: _*)
  }

  def fatal(err: Throwable) {
    val msg = err.getMessage + "\n" + err.getStackTraceString
    if (useColor) red(msg)
    else tagged("ERROR", msg)
  }

  def error(subject: Any, args: Any*) {
    subject match {
      case err: Throwable ⇒
        val msg = err.getMessage
        if (useColor) red(msg)
        else tagged("ERROR", msg)

      case format: String ⇒
        if (useColor) red(format, args: _*)
        else tagged("ERROR", format, args: _*)
    }
  }

  def info(format: String, args: Any*) {
    if (useColor) green(format, args: _*)
    else tagged("INFO", format, args: _*)
  }

  def warn(format: String, args: Any*) {
    if (useColor) yellow(format, args: _*)
    else tagged("WARN", format, args: _*)
  }

  def debug(format: String, args: Any*) {
    if (useColor) cyan(format, args: _*)
    else tagged("DEBUG", format, args: _*)
  }
  
  def yellow(format: String, args: Any*) {
    colored(Console.YELLOW, format, args: _*)
  }
  
  def cyan(format: String, args: Any*) {
    colored(Console.CYAN, format, args: _*)
  }
  
  def magenta(format: String, args: Any*) {
    colored(Console.MAGENTA, format, args: _*)
  }
  
  def red(format: String, args: Any*) {
    colored(Console.RED, format, args: _*)
  }
  
  def green(format: String, args: Any*) {
    colored(Console.GREEN, format, args: _*)
  }
  
  def white(format: String, args: Any*) {
    colored(Console.WHITE, format, args: _*)
  }
  
}
