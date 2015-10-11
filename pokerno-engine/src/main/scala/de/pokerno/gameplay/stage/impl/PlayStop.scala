package de.pokerno.gameplay.stage.impl

import de.pokerno.gameplay.{Events, Stage}

private[gameplay] case class PlayStop(ctx: Stage.Context) extends Stage {

  import ctx.gameplay._

  def apply() = {
    play.stop()
    events broadcast Events.playStop()
  }

}
