package pokerno.backend.cli

import pokerno.backend.model._
import scala.math.{ BigDecimal => Decimal }

case class Config(
  val betSize: Decimal = .0,
  val tableSize: Int = 6,
  val mixedGame: Option[Game.Mixed] = None,
  val limitedGame: Option[Game.Limited] = None
)

object Main {
  
  val parser = new scopt.OptionParser[Config]("poker-console") {
    opt[Int]('t', "table-size") action { (value, c) => c.copy(tableSize = value) } text("Table size")
    opt[Decimal]('b', "bet-size") action { (value, c) => c.copy(betSize = value) } text("Bet size")
    opt[String]("mix") action { (value, c) => c.copy(limitedGame = Some(value)) } text("Mixed game")
    opt[String]('g', "game") action { (value, c) => c.copy(mixedGame = Some(value)) } text("Limited game")
  }
  
  def main(args: Array[String]) {
    parser.parse(args, Config()) map { config =>
      Console printf("Hello, world!")
    }
  }
}

/*

package main

//
// command line play
//
import (
  "flag"
  "fmt"
  "log"
  "os"
)

import (
  "gopoker/event"
  "gopoker/message"

  "gopoker/model"
  "gopoker/model/game"

  "gopoker/engine"
  "gopoker/console"
)

var (
  logfile     = flag.String("logfile", "", "Log file path")
  betsize     = flag.Float64("betsize", 20., "Bet size")
  tablesize   = flag.Int("tablesize", 6, "Table size")
  mixedGame   = flag.String("mix", "", "Mix to play")
  limitedGame = flag.String("game", "Texas", "Game to play")
)

const (
  defaultConfigDir = "/etc/gopoker"
)

var (
  configDir = flag.String("config-dir", defaultConfigDir, "Config dir")
)

func main() {
  flag.Parse()
  model.LoadGames(*configDir)

  if *logfile != "" {
    log := createLog()
    defer log.Close()
  }

  instance := createInstance()
  fmt.Printf("%+v\n", instance)

  me := make(event.Channel, 100)
  joinInstance(instance, me)

  instance.Start()

  session := console.Play{instance}
  session.Start(me)
}

func createLog() *os.File {
  w, err := os.Create(*logfile)

  if err != nil {
    panic(err.Error())
  }

  log.SetOutput(w)

  return w
}

func createInstance() *engine.Instance {
  ctx := &engine.Context{
    Stake: model.NewStake(*betsize),
    Table: model.NewTable(*tablesize),
  }

  if *mixedGame != "" {
    ctx.Mix = model.NewMix(game.MixedGame(*mixedGame), *tablesize)
  } else {
    ctx.Game = model.NewGame(game.LimitedGame(*limitedGame), game.FixedLimit, *tablesize)
  }

  return engine.NewInstance(ctx)
}

func joinInstance(instance *engine.Instance, me event.Channel) {
  players := []string{"A", "B", "C", "D", "E", "F", "G", "H", "I"}
  stack := 1500.

  for i, player := range players {
    if i >= instance.Table.Size {
      break
    }
    instance.Broker().Bind(player, me)
    instance.JoinTable(&message.Join{model.Player(player), i, stack})
  }
}
*/