package de.pokerno.replay

import de.pokerno.poker.{ Card, Deck }
import de.pokerno.model.{ Dealer, Player, Table, Stake, Variation, Game, Bet, Seat, Street }
import de.pokerno.replay._
import akka.actor.{ Actor, ActorSystem, ActorLogging, ActorRef, Props, Kill }
import de.pokerno.format.text

private[replay] object Replayer {
  case class Replay(scenario: Scenario)
}

private[replay] case class ReplayError(msg: String) extends Exception(msg)

private[replay] class Replayer(node: ActorRef) extends Actor {
  import io.netty.handler.codec.http
  import io.netty.channel.{ ChannelHandlerContext, ChannelFutureListener }
  import http.HttpHeaders._

  def receive = {
    // http request
    case (id: String, content: String, ctx: ChannelHandlerContext, resp: http.DefaultFullHttpResponse) ⇒
      resp.headers().add(Names.CONTENT_TYPE, "application/json")

      def sendError(err: Throwable) {
        resp.setStatus(http.HttpResponseStatus.UNPROCESSABLE_ENTITY)
        resp.content().writeBytes(
          """{"status": "error", "error": "%s"}""".format(err.getMessage).getBytes)
      }

      try {
        val src = scala.io.Source.fromString(content)
        val scenario = Scenario.parse(id, src)
        replay(scenario)
        resp.content().writeBytes("""{"status": "ok"}""".getBytes)
      } catch {
        case err: ReplayError ⇒
          sendError(err)

        case err: text.ParseError ⇒
          sendError(err)

        case err: Throwable ⇒
          err.printStackTrace()
          sendError(new Exception("something went wrong"))
      }

      ctx.channel.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE)

    // console request
    case Replayer.Replay(scenario) ⇒
      try {
        replay(scenario)
      } catch {
        case err: Throwable ⇒
          Console printf ("[ERROR] %s%s%s\n", Console.RED, err.getMessage, Console.RESET)
      }

    case _ ⇒
  }

  import context._

  private def replay(scenario: Scenario) {
    val table = scenario.table.getOrElse(
      throw ReplayError("table not defined"))
    val variation = scenario.variation.getOrElse(
      throw ReplayError("game not defined"))
    val stake = scenario.stake.getOrElse(
      throw ReplayError("stake not defined"))

    val deck = scenario.deck

    val replay = system.actorOf(Props(classOf[Replay], scenario.name, table, variation, stake, deck))
    replay ! Replay.Observe(node)

    // table.seats.zipWithIndex foreach { case (seat, pos) =>
    //  if (!seat.isEmpty)
    //    replay ! JoinPlayer(pos, seat.player.get, Some(seat.stack))
    // }

    for (streetName ← scenario.streets) {
      val street: Option[Street.Value] = streetName
      if (street.isDefined)
        replay ! Replay.Street(
              street.get,
              scenario.actions(streetName).toSeq,
              scenario.speed)
    }
    if (scenario.showdown) replay ! Replay.Showdown
    if (!scenario.paused) replay ! Replay.Stop
    else replay ! Kill
  }
}
