package de.pokerno.model.seat

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import de.pokerno.model.Seat

sealed class AutoPlay(pos: Int) extends Seat(pos) {
}
