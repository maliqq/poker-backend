package de.pokerno.model.seat.impl

object Callbacks {

  trait CallbackTopic
  case object Before extends CallbackTopic
  case object After extends CallbackTopic
  case object On extends CallbackTopic

  class Callbacks[T] {
    type cb = (T, T) ⇒ Unit
    private val _cb = collection.mutable.HashMap[CallbackTopic, collection.mutable.ListBuffer[cb]]()

    def bind(topic: CallbackTopic)(f: (T, T) ⇒ Unit): Unit = {
      if (!_cb.contains(topic)) _cb(topic) = collection.mutable.ListBuffer[cb]()
      _cb(topic) += f
    }

    def unbind(topic: CallbackTopic, f: (T, T) ⇒ Unit): Unit = if (_cb.contains(topic))
      _cb(topic) -= f

    def fire(topic: CallbackTopic, _old: T, _new: T): Unit = if (_cb.contains(topic))
      _cb(topic).foreach { _(_old, _new) }

    def clear(topic: CallbackTopic): Unit =
      _cb(topic) = collection.mutable.ListBuffer[cb]()

    def before(_old: T, _new: T): Unit = fire(Before, _old, _new)
    def after(_old: T, _new: T): Unit = fire(After, _old, _new)
    def on(_old: T, _new: T): Unit = fire(On, _old, _new)

  }
}
