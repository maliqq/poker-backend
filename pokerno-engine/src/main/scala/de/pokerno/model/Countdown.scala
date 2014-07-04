package de.pokerno.model

import com.fasterxml.jackson.annotation.{JsonValue, JsonInclude}
import akka.actor.{Actor, Cancellable, Scheduler}
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

class Pausable(timers: Timers, initialDuration: FiniteDuration) {
  private var ticked = FiniteDuration(0, MILLISECONDS)
  
  private var started: java.time.Instant = null
  private var _timer: Cancellable = null
  
  start()
  
  def cancel() = _timer.cancel()
  
  def pause() = {
    _timer.cancel()
    ticked += FiniteDuration(java.time.Instant.now().toEpochMilli() - started.toEpochMilli(), MILLISECONDS)
  }
  
  def resume() = {
    restart()
  }
  
  def left = initialDuration - ticked
  
  private def restart() {
    restart(left)
  }
  
  private def restart(left: FiniteDuration) {
    started = java.time.Instant.now()
    _timer = start(left)
  }
  
  private def start() {
    restart(initialDuration)
  }
  
  private def start(duration: FiniteDuration) = timers.after(duration) {
    done()
    cleanup()
  }
  
  def done() = {}
  def cleanup() = {}
}

class Countdown(name: String, duration: FiniteDuration) {
  private var timer: Option[Pausable] = None
  
  def start(timers: Timers)(f: => Unit) {
    timer = Some(new Pausable(timers, duration) {
      override def done = f
      override def cleanup = {
        timer = None
      }
    })
  }
  
  def pause() {
    timer.map { t =>
      t.pause()
      info("[countdown] Pausing %s; %s left", name, t.left)
    }
  }

  def resume() {
    timer.map { t =>
      info("[countdown] resuming %s; %s left", name, t.left)
      t.resume()
    }
  }
  
  def cancel() {
    timer.map { t =>
      info("[countdown] Cancelling %s", name)
      t.cancel()
      timer = None
    }
  }

  def done() {
    timer.map { t =>
      info("[countdown] Done %s", name)
      t.cancel()
      t.done()
      timer = None
    }
  }

}
