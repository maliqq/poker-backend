package de.pokerno.format.text

import de.pokerno.model
import de.pokerno.poker
import util.matching.Regex

object Lexer {
  case class Tag(name: String) extends annotation.StaticAnnotation
  
  object Conversions {
    implicit def string2int(s: String) = Integer.parseInt(s)
    implicit def string2cards(s: String) = poker.Cards(s)
    implicit def string2quotedString(s: String) = new QuotedString(s)
  }
  
  class QuotedString(v: String) {
    override def toString = v
  }
  
  trait Token
  
  object Tags {
    import Conversions._
    
    @Tag(name = "TABLE")
    case class Table(uuid: QuotedString, max: Int) extends Token {
      def this(params: Array[String]) = this(params(0), params(1).replace("-max", ""))
    }
    
    @Tag(name = "SEAT")
    case class Seat(playerUuid: QuotedString, stack: Int) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
    
    object StakeUtil {
      def fromParams(params: Array[String]): Tuple3[Int, Int, Option[Int]] = {
        val d = params(0).split("/")
        println("HERE")
        d.length match {
          case 2 =>
            (d(0), d(1), None)
          case 3 =>
            (d(0), d(1), Some(d(2)))
          case _ =>
            throw new IllegalArgumentException("STAKE requires 2 or 3 arguments")
        }
      }
    }
    
    @Tag(name = "STAKE")
    case class Stake(sb: Int, bb: Int, ante: Option[Int]) extends Token {
      def this(args: Tuple3[Int, Int, Option[Int]]) = this(args._1, args._2, args._3)
      def this(params: Array[String]) = this(StakeUtil.fromParams(params))
    }
    
    @Tag(name = "GAME")
    case class Game(variation: model.Variation, limit: model.Game.Limit) extends Token {
      def this(params: Array[String]) = this(null, null)
    }
    
    @Tag(name = "SPEED")
    case class Speed(interval: Int) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "BUTTON")
    case class Button(pos: Int) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "STREET")
    case class Street(name: String) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "SB")
    case class Sb(player: QuotedString) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "BB")
    case class Bb(player: QuotedString) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "ANTE")
    case class Ante(player: QuotedString) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "RAISE")
    case class Raise(player: QuotedString, amount: Int) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
    
    @Tag(name = "ALLIN")
    case class AllIn(player: QuotedString) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "CALL")
    case class Call(player: QuotedString, amount: Int) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
    
    @Tag(name = "FOLD")
    case class Fold(player: QuotedString) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "DEAL")
    case class Deal(player: QuotedString, cards: List[poker.Card], cardsNum: Integer) extends Token {
      def this(params: Array[String]) = this(params(0), params(1), null)
    }
    
    @Tag(name = "JSON")
    case class Json(data: QuotedString) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "CHAT")
    case class Chat(player: QuotedString, message: String) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
    
    @Tag(name = "DISCARD")
    case class Discard(player: QuotedString, cards: List[poker.Card]) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
    
    @Tag(name = "SHOW")
    case class Show(player: String, cards: List[poker.Card]) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
    
    @Tag(name = "MUCK")
    case class Muck(player: QuotedString, cards: List[poker.Card]) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
    
    @Tag(name = "SLEEP")
    case class Sleep(time: Int) extends Token {
      def this(params: Array[String]) = this(params(0))
    }
    
    @Tag(name = "TIMER")
    case class Timer(player: QuotedString, left: Int) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
  }
  
  
  import reflect.runtime.{ universe => ru }
  import reflect.runtime.{ currentMirror => cm }
  
  val tagsReflection = cm.reflect(Tags)
  val tags = tagsReflection.symbol.typeSignature.declarations.filter(_.isClass)
  val tagClasses = tags.map { decl =>
    val name = decl.annotations.head.scalaArgs(0).asInstanceOf[ru.Literal]
    name match {
      case ru.Literal(ru.Constant(s: String)) =>
        s -> decl
      case _ =>
        name.toString -> decl
    }
  }.toMap
  
  Console printf("tagClasses=%s\n", tagClasses)

}
