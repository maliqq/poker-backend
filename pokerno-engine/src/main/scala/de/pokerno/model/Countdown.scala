package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonValue, JsonInclude}
import akka.actor.{Cancellable, Scheduler}
import concurrent.duration._
import de.pokerno.util.Colored._

abstract class Timers {
  type Timer = Cancellable
  
  def every(duration: FiniteDuration, delay: FiniteDuration)(f: => Unit): Timer
  def after(duration: FiniteDuration)(f: => Unit): Timer
}

class AkkaTimers(scheduler: Scheduler, ec: concurrent.ExecutionContext) extends Timers {
  def every(duration: FiniteDuration, delay: FiniteDuration)(f: => Unit) = {
    scheduler.schedule(duration, delay)(f)(ec)
  }
  
  def after(duration: FiniteDuration)(f: => Unit) = {
    scheduler.scheduleOnce(duration)(f)(ec)
  }
}

abstract class Timer(timers: Timers) {
  var timer: Cancellable = create()
  
  def create(): Cancellable
  def done(): Unit
  
  def cancel() = timer.cancel()
  def resume() = timer = create()
  
  def destroy() = {
    cancel()
    done()
  }
  
}

class Countdown(val name: String, capacity: Int) {
  private var _clock = capacity
  @JsonValue def clock: Option[Int] = if (timer.isDefined) Some(_clock) else None
  
  var timer: Option[Timer] = None
  
  def start(timers: Timers)(f: => Unit) {
    _clock = capacity
    timer = Some(new Timer(timers) {
      def create() = timers.every(1 second, 1 second) {
        tick()
      }
      
      def done() = f
    })
  }
  
  def cancel() {
    info("[countdown] Cancelling %s", name)
    _cancel()
    _clock = 0
    timer = None
  }
  
  def pause() {
    info("[countdown] Pausing %s; %ds left", name, _clock)
    _cancel()
  }
  
  private def _cancel() {
    timer.map(_.cancel())
  }
  
  def resume() {
    if (_clock > 0) {
      info("[countdown] resuming %s; %ds left", name, _clock)
      timer.map(_.resume())
    }
  }
  
  def tick() {
    _clock -= 1
    if (_clock <= 0) destroy()
  }
  
  def destroy() {
    info("[countdown] %s done", name)
    timer.map(_.destroy())
    timer = None
  }
   
}
