package de.pokerno.format.text

import util.matching.Regex

object Parser {
  
  def parseTagAndParams(line: String): Tuple2[String, Array[String]] = {
    val p = """^([A-Z]+)(?:\s+(.+))?$""".r
    line.stripLineEnd match {
      case p(t: String, s: String) =>
        (t, """\s+""".r.split(s))
      case s: String =>
        (s, null)
      case _ =>
        (null, null)
    }
  }
  
  import reflect.runtime.{ currentMirror => cm }
  import reflect.runtime.{ universe => ru }
  
  def parseLine(line: String): Option[Lexer.Token] = {
    if (line.startsWith("#") || line.matches("^\\s*$")) return None
    val parts = line.split("#", 2)
    val (tag, params) = parseTagAndParams(parts(0))
    if (tag == null) return None
    
    val decl = Lexer.tagClasses.get(tag)
    if (decl.isDefined) {
      try {
        val m = decl.get.typeSignature.declaration(ru.nme.CONSTRUCTOR).asTerm.alternatives.last
        val result = cm.reflectClass(decl.get.asClass).reflectConstructor(m.asMethod)(params)
        return Some(result.asInstanceOf[Lexer.Token])
      } catch {
      case e: Throwable =>
        e.printStackTrace()
        Console printf("can't parse: %s with type: %s\n", line, decl.get)
        return None
      }
    } else None
  }
  
  def parse(lines: List[String]): List[Lexer.Token] = {
    lines.map(parseLine(_)).filter(_.isDefined).map(_.get)
  }
  
  def parse(src: scala.io.Source): List[Lexer.Token] = {
    parse(src.getLines.toList)
  }
  
}
