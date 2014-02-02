package de.pokerno.format.text

import util.matching.Regex

case class ParseError(msg: String) extends Exception(msg)

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

  type ParsedLine = Tuple3[String, Int, Lexer.Token]
  
  def parseLine(line: String, lineno: Int): Option[ParsedLine] = {
    if (line.startsWith("#") || line.matches("^\\s*$")) return None
    val parts = line.split("#", 2)
    val (tag, params) = parseTagAndParams(parts(0))
    if (tag == null) return None
    
    val decl = Lexer.tagClasses.get(tag)
    if (decl.isDefined) {
      try {
        val m = decl.get.typeSignature.declaration(ru.nme.CONSTRUCTOR).asTerm.alternatives.last
        val result = cm.reflectClass(decl.get.asClass).reflectConstructor(m.asMethod)(params)
        Some((line, lineno, result.asInstanceOf[Lexer.Token]))
      
      } catch {
      
      case e: Throwable =>
        throw ParseError("Can't parse; error at line %d type %s: %s".format(lineno, decl, e.getMessage))
      }
    } else None
  }
  
  def parse(lines: List[String]): List[ParsedLine] = {
    lines.zipWithIndex.map { case (line, i) =>
      parseLine(line, i + 1)
    }.filter(_.isDefined).map(_.get)
  }
  
  def parse(src: scala.io.Source): List[ParsedLine] = {
    parse(src.getLines().toList)
  }
  
}
