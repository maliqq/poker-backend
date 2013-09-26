package pokerno.backend.model

case class Ranking
case class LimitedGame

class Game(var game: LimitedGame, var limit: Limit, var tableSize: Int) {
  var hasBlinds: Boolean = false
  var hasAnte: Boolean = false
  var hasBringIn: Boolean = false
  var hasBoard: Boolean = false
  var hasVela: Boolean = false
  
  var discards: Boolean = false
  var reshuffle: Boolean = false
  
  val DEFAULT_MAX_TABLE_SIZE = 10
  var maxTableSize: Int = DEFAULT_MAX_TABLE_SIZE
  
  var pocketSize: Int = 0
  var streetsNum: Int = 0
  var hiRanking: Ranking = new Ranking()
  var loRanking: Ranking = new Ranking()
  
  var defaultLimit: Limit = new Limit()
}

/*
package model

import (
	"fmt"
	"encoding/json"
)

import (
	"github.com/golang/glog"
)

import (
	"gopoker/model/game"
	"gopoker/poker/hand"
)

// GameOptions - game options
type GameOptions struct {
	Group game.Group

	HasBlinds  bool
	HasAnte    bool
	HasBringIn bool
	HasBoard   bool
	HasVela    bool

	Discards  bool
	Reshuffle bool

	MaxTableSize int
	PocketSize   int
	Streets      int // number of streets till showdown

	Hi hand.Ranking
	Lo hand.Ranking

	DefaultLimit game.Limit
}

// Game - game
type Game struct {
	Type         game.LimitedGame
	Limit        game.Limit
	TableSize    int
	*GameOptions `json:"-"`
}

// Variation - union of Game and Mix
type Variation interface {
	IsMixed() bool
}

// NewGame - create game
func NewGame(gameType game.Type, limit game.Limit, tableSize int) *Game {
	limitedGame, success := gameType.(game.LimitedGame)

	if !success {
		glog.Fatalf("can't create game with: %#v", gameType)
	}

	options := gameOptions(limitedGame)

	maxTableSize := options.MaxTableSize
	if tableSize == 0 || tableSize > maxTableSize {
		tableSize = maxTableSize
	}

	if limit == "" {
		limit = options.DefaultLimit
	}

	game := &Game{
		Type:        limitedGame,
		Limit:       limit,
		TableSize:   tableSize,
		GameOptions: options,
	}

	return game
}

func gameOptions(limitedGame game.LimitedGame) *GameOptions {
	gameOptions, success := Games[limitedGame]
	if !success {
		glog.Fatalf("can't find options for: %#v", limitedGame)
	}

	return gameOptions
}

// IsMixed - false
func (game *Game) IsMixed() bool {
	return false
}

// String - game to string
func (game *Game) String() string {
	return fmt.Sprintf("%s %s", game.Type, game.Limit)
}

func (g *Game) UnmarshalJSON(data []byte) error {
	var result struct {
		Type      string
		Limit     string
		TableSize int
	}

	err := json.Unmarshal(data, &result)

	if err != nil {
		return err
	}

	*g = *NewGame(game.LimitedGame(result.Type), game.Limit(result.Limit), result.TableSize)

	return nil
}

*/