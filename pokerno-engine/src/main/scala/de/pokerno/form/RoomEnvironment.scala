package de.pokerno.form

import akka.actor.ActorRef

case class RoomEnvironment(
    balance: de.pokerno.payment.thrift.Payment.FutureIface,
    pokerdb: Option[de.pokerno.data.pokerdb.thrift.PokerDB.FutureIface] = None,
    history: Option[ActorRef] = None,
    persist: Option[ActorRef] = None
) {

}
