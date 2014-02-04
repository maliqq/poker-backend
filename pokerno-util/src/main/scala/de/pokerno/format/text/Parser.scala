package de.pokerno.format.text

import util.matching.Regex

case class ParseError(msg: String) extends Exception(msg)

object Parser {
  private final val lineAndThenParams = """^([A-Z]+)(?:\s+(.+))?$""".r
  private final val shellWords = "[^\\s\"]+|\"([^']*)\"".r
  
  def parseTagAndParams(line: String): Tuple2[String, Array[String]] = {
    line.stripLineEnd match {
      case lineAndThenParams(t: String, s: String) =>
        (t, shellWords.findAllIn(s).toArray)
        
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
        e.printStackTrace()
        throw ParseError("Can't parse %s; error at line %d: %s".format(decl.get, lineno, e.getMessage))
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
