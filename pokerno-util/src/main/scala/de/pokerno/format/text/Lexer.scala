package de.pokerno.format.text

import de.pokerno.protocol.wire
import util.matching.Regex
import proto.wire.GameSchema

object Lexer {
  case class Tag(name: String) extends annotation.StaticAnnotation

  object Conversions {
    implicit def string2int(s: String) = Integer.parseInt(s)
    // FIXME remove this shit
    final val a = "23456789TJQKA"
    final val b = "shdc"
    implicit def string2cards(s: String): Array[Byte] = {
      val tuples = for {
        m ← "(..)".r findAllIn s
      } yield ((m.charAt(0), m.charAt(1)))
      tuples.map[Integer] {
        case (chr1, chr2) ⇒
          val k = a.indexOf(chr1)
          val s = b.indexOf(chr2)
          if (k != -1 && s != -1) (k << 2) + s + 1
          else null
      } filter (_ != null) map (_.toByte) toArray
    }
    implicit def string2quotedString(s: String) = new QuotedString(s)

    implicit def string2gametype(s: String): GameSchema.GameType = s match {
      case "texas" ⇒ GameSchema.GameType.TEXAS
      case "omaha" ⇒ GameSchema.GameType.OMAHA
      // ...
      case x       ⇒ throw ParseError("unknown game: %s" format x)
    }

    implicit def string2gamelimit(s: String): GameSchema.GameLimit = s match {
      case "NL" | "no-limit"    ⇒ GameSchema.GameLimit.NL
      case "FL" | "fixed-limit" ⇒ GameSchema.GameLimit.FL
      // FIXME turn on later
      //case "PL" | "pot-limit" => wire.GameSchema.GameLimit.PL
      case x                    ⇒ throw ParseError("unknown limit: %s" format x)
    }
  }

  class QuotedString(v: String) {
    def unquote = {
      val (start, end) = {
        var start = 0
        var end = v.length
        if (v.startsWith("\""))
          start = 1
        if (v.endsWith("\""))
          end = v.length - 1
        (start, end)
      }
      v.substring(start, end)
    }

    override def toString = unquote
  }

  trait Token
  trait BettingSemantic

  object Tags {
    import Conversions._

    @Tag(name = "TABLE")
    case class Table(id: QuotedString, max: Int) extends Token {
      def this(params: Array[String]) = this(params(0), params(1).replace("-max", ""))
    }

    @Tag(name = "SEAT")
    case class Seat(pos: Int, playerUuid: QuotedString, stack: Int) extends Token {
      def this(params: Array[String]) = this(params(0).replace(":", ""), params(1), params(2))
    }

    object Stake {
      private[text] def fromParams(params: Array[String]): Tuple3[Int, Int, Option[Int]] = {
        val d = params(0).split("/")
        d.length match {
          case 2 ⇒
            val b = List[Int](d(0), d(1))
            (b.min, b.max, None)
          case 3 ⇒
            val b = List[Int](d(0), d(1))
            (b.max, b.max, Some(d(2)))
          case _ ⇒
            throw new IllegalArgumentException("STAKE requires 2 or 3 arguments")
        }
      }
    }

    @Tag(name = "STAKE")
    case class Stake(sb: Int, bb: Int, ante: Option[Int]) extends Token {
      def this(args: Tuple3[Int, Int, Option[Int]]) = this(args._1, args._2, args._3)
      def this(params: Array[String]) = this(Stake.fromParams(params))
    }

    @Tag(name = "GAME")
    case class Game(game: GameSchema.GameType, limit: GameSchema.GameLimit) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }

    @Tag(name = "SPEED")
    case class Speed(interval: Int) extends Token {
      def this(params: Array[String]) = this(params(0))
    }

    @Tag(name = "DECK")
    case class Deck(cards: Array[Byte]) extends Token {
      def this(params: Array[String]) = this(new QuotedString(params(0)).unquote)
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
    case class Sb(player: QuotedString) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0))
    }

    @Tag(name = "BB")
    case class Bb(player: QuotedString) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0))
    }

    @Tag(name = "BLINDS")
    case class Blinds() extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this()
    }

    @Tag(name = "ANTE")
    case class Ante(player: QuotedString) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0))
    }

    @Tag(name = "ANTES")
    case class Antes() extends Token with BettingSemantic {
      def this(params: Array[String]) = this()
    }

    @Tag(name = "RAISE")
    case class Raise(player: QuotedString, amount: Int) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0), params(1))
    }

    @Tag(name = "ALLIN")
    case class AllIn(player: QuotedString) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0))
    }

    @Tag(name = "CHECK")
    case class Check(player: QuotedString) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0))
    }

    @Tag(name = "CALL")
    case class Call(player: QuotedString, amount: Int) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0), params(1))
    }

    @Tag(name = "FOLD")
    case class Fold(player: QuotedString) extends Token
        with BettingSemantic {
      def this(params: Array[String]) = this(params(0))
    }

    object Deal {
      private[text] def fromParams(params: Array[String]): Tuple3[QuotedString, Array[Byte], Integer] = {
        if (params.length == 1) // board
          return (null, params(0), null)
        if (params.length == 2 && params(1).matches("^\\d+$"))
          return (params(0), null, Integer.parseInt(params(1)))
        (params(0), params(1), null)
      }
    }

    @Tag(name = "DEAL")
    case class Deal(player: QuotedString, cards: Array[Byte], cardsNum: Integer) extends Token {
      def this(args: Tuple3[QuotedString, Array[Byte], Integer]) = this(args._1, args._2, args._3)
      def this(params: Array[String]) = this(Deal.fromParams(params))
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
    case class Discard(player: QuotedString, cards: Array[Byte]) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }

    @Tag(name = "SHOW")
    case class Show(player: String, cards: Array[Byte]) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }

    @Tag(name = "MUCK")
    case class Muck(player: QuotedString, cards: Array[Byte]) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }

    @Tag(name = "SLEEP")
    case class Sleep(time: Int) extends Token {
      def this(params: Array[String]) = this(params(0))
    }

    @Tag(name = "SHOWDOWN")
    case class Showdown() extends Token {
      def this(params: Array[String]) = this()
    }

    @Tag(name = "TIMER")
    case class Timer(player: QuotedString, left: Int) extends Token {
      def this(params: Array[String]) = this(params(0), params(1))
    }
  }

  import reflect.runtime.{ universe ⇒ ru }
  import reflect.runtime.{ currentMirror ⇒ cm }

  private val tagsReflection = cm.reflect(Tags)
  private val tags = tagsReflection.symbol.typeSignature.declarations.filter(_.isClass)
  private[text] val tagClasses = tags.map { decl ⇒
    val name = decl.annotations.head.scalaArgs(0).asInstanceOf[ru.Literal]
    name match {
      case ru.Literal(ru.Constant(s: String)) ⇒
        s -> decl
      case _ ⇒
        name.toString -> decl
    }
  }.toMap

  //Console printf("tagClasses=%s\n", tagClasses)

}
