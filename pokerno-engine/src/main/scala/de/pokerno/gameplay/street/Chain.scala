package de.pokerno.gameplay.street

import de.pokerno.gameplay.{ Event, Stage, StageContext, Street, Streets, StreetStage, StreetOptions }

private[gameplay] class Chain(
    val ctx: StageContext,
    streetOptions: Map[Street.Value, StreetOptions]) {
  
  import ctx.gameplay._
  
  private val streets = Street.byGameGroup(game.options.group)
  private val iterator = streets.iterator
  private var _current: Option[Street.Value] = None

  def current = _current

  def apply() = if (iterator.hasNext) {
    val street = iterator.next()
    val stage = new StreetStage(street, streetOptions(street))
    _current = Some(street)

    ctx broadcast Event.streetStart(street)

    stage(ctx) match {
      case Stage.Next | Stage.Skip ⇒
        ctx.ref ! Streets.Next

      case Stage.Wait ⇒
        println("waiting")

      case x ⇒
        throw new MatchError("unhandled stage transition: %s".format(x))
    }

  } else ctx.ref ! Streets.Done

  override def toString = {
    val b = new StringBuilder
    b.append("#[StreetChain")
    for (streetOption ← streetOptions) {
      b.append(" " + streetOption.toString)
    }
    b.append("]").toString()
  }
}
