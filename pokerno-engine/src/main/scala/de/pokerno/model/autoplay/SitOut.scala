package de.pokerno.model.autoplay

import de.pokerno.model.table.seat.States

trait SitOut { s: States =>
  
  private var _sittingOut = false
  
  def isSittingOut = _sittingOut
  def toggleSittingOut() {
    _sittingOut = !_sittingOut
  }
  
}
