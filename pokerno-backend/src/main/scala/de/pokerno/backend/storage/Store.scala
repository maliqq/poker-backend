package de.pokerno.backend.storage

object Store {

  abstract class Client {
    def write(entry: PlayHistory.Entry)
  }

}
