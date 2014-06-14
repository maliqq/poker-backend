package de.pokerno.model

import de.pokerno.poker.Hand
import math.{ BigDecimal ⇒ Decimal }

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonValue, JsonProperty, JsonCreator}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

object Game {

  trait Group
  case object Holdem extends Group
  case object SevenCard extends Group
  case object SingleDraw extends Group
  case object TripleDraw extends Group

  final val MaxTableSize = 10
  final val FullBoardSize = 5

  case class Options(
      group: Group = Holdem,
      hasBlinds: Boolean = false,
      hasAnte: Boolean = false,
      hasBringIn: Boolean = false,
      hasBoard: Boolean = false,
      hasVela: Boolean = false,

      discards: Boolean = false,
      reshuffle: Boolean = false,

      maxTableSize: Int = MaxTableSize,

      pocketSize: Int = 0,
      streetsNum: Int = 0,
      hiRanking: Option[Hand.Ranking] = None,
      loRanking: Option[Hand.Ranking] = None,

      defaultLimit: Limit = Limit.None) {

    // на случай когда колода задается вручную
    def minDeckSize = {
      var n = maxTableSize * pocketSize
      if (hasBoard) n += 5
      if (discards) n += pocketSize
      n
    }

    override def toString = {
      val b = new StringBuilder

      b.append(" group=%s max-table-size=%d pocket-size=%d streets-num=%d default-limit=%s".format(
        group,
        maxTableSize,
        pocketSize,
        streetsNum,
        defaultLimit))

      if (hiRanking.isDefined) b.append(" hi-ranking=%s", hiRanking.get)
      if (loRanking.isDefined) b.append(" lo-ranking=%s", loRanking.get)

      if (hasBlinds)    b.append(" ✓blinds")
      if (hasAnte)      b.append(" ✓ante")
      if (hasBringIn)   b.append(" ✓bring-in")
      if (hasBoard)     b.append(" ✓board")
      if (hasVela)      b.append(" ✓vela")
      if (discards)     b.append(" ✓discards")
      if (reshuffle)    b.append(" ✓reshuffle")

      b.toString
    }
  }
  
  @JsonCreator
  def apply(@JsonProperty("type") `type`: String, @JsonProperty("limit") limit: String, @JsonProperty("tableSize") tableSize: Option[Int]): Game = {
    Console printf("type=%s limit=%s size=%d", `type`, limit, tableSize)
    new Game(`type`: GameType, limit: Limit, tableSize.get) // FIXME None.get
  }
  
  def apply(`type`: GameType, limit: Option[Limit] = None, tableSize: Option[Int] = None): Game = {
    val options = Games(`type`)
    
    val _limit: Limit = limit match {
      case None    ⇒ options.defaultLimit
      case Some(l) ⇒ l
    }
    
    val _tableSize = tableSize match {
      case None ⇒ options.maxTableSize
      case Some(size) ⇒
        if (size > options.maxTableSize)
          options.maxTableSize
        else
          size
    }
    
    new Game(`type`, _limit, _tableSize)
  }
  
  def apply(`type`: GameType, limit: Limit): Game =
    Game(`type`, Some(limit), None)
    
  def apply(`type`: GameType, tableSize: Int): Game =
    Game(`type`, None, Some(tableSize))
  
}

class GameBuilder {
  @JsonProperty var `type`: String = null
  @JsonProperty var limit: String = null
  @JsonProperty var tableSize: Integer = null
  
  def build(): Game = Game(`type`: GameType, Option(limit: Limit), Option[Int](tableSize))
}

@JsonDeserialize(builder = classOf[GameBuilder])
class Game(
    @JsonProperty val `type`: GameType,
    @JsonProperty val limit: Limit,
    @JsonProperty val tableSize: Int
  ) extends Variation {
  
  def options = `type`.options
  
  override def toString = "%s %s %s-max" format (`type`, limit, tableSize)
}
