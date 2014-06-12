package de.pokerno.util

object Colored {
  private val useColor = (System.getProperty("log.text") == null)

  private def colored(color: String, format: String, args: Any*) {
    Console printf (color + format + "\n" + Console.RESET, args: _*)
  }

  private def tagged(tag: String, format: String, args: Any*) {
    Console printf ("[" + tag + "] " + format + "\n", args: _*)
  }

  def fatal(err: Throwable) = {
    val msg = err.getMessage + "\n" + err.getStackTraceString
    if (useColor) colored(Console.RED, msg)
    else tagged("ERROR", msg)
  }

  def error(subject: Any, args: Any*) =
    subject match {
      case err: Throwable ⇒
        val msg = err.getMessage
        if (useColor) colored(Console.RED, msg)
        else tagged("ERROR", msg)

      case format: String ⇒
        if (useColor) colored(Console.RED, format, args: _*)
        else tagged("ERROR", format, args: _*)
    }

  def info(format: String, args: Any*): Unit =
    if (useColor) colored(Console.MAGENTA, format, args: _*)
    else tagged("INFO", format, args: _*)

  def warn(format: String, args: Any*): Unit =
    if (useColor) colored(Console.YELLOW, format, args: _*)
    else tagged("WARN", format, args: _*)

  def debug(format: String, args: Any*): Unit =
    if (useColor) colored(Console.CYAN, format, args: _*)
    else tagged("DEBUG", format, args: _*)

}
