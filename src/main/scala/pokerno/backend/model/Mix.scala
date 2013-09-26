package pokerno.backend.model

class Mix {

}
/*package model

import (
	"encoding/json"
	"fmt"
)

import (
	"github.com/golang/glog"
)

import (
	"gopoker/model/game"
)

const (
	// MixedGameMaxTableSize - table size for mixed game
	MixedGameMaxTableSize = 8
)

// MixOptions - mix options
type MixOptions struct {
	Type game.LimitedGame
	game.Limit
}

// Mix - mix
type Mix struct {
	Type  game.MixedGame
	Games []*Game `json:"-"`
}

// NewMix - create mix
func NewMix(gameType game.Type, tableSize int) *Mix {
	mixedGame, success := gameType.(game.MixedGame)

	if !success {
		glog.Fatalf("can't create mix for: %#v\n", gameType)
	}

	options := mixOptions(mixedGame)

	maxTableSize := MixedGameMaxTableSize
	if tableSize == 0 || tableSize > maxTableSize {
		tableSize = maxTableSize
	}

	games := make([]*Game, len(options))
	for i, mixOptions := range options {
		games[i] = NewGame(mixOptions.Type, mixOptions.Limit, tableSize)
	}

	mix := &Mix{
		Type:  mixedGame,
		Games: games,
	}

	return mix
}

func mixOptions(g game.MixedGame) []MixOptions {
	options, success := Mixes[g]
	if !success {
		glog.Fatalf("can't populate mix options with: %#v\n", g)
	}

	return options
}

// String - mix to string
func (mix *Mix) String() string {
	return fmt.Sprintf("%s", mix.Type)
}

// IsMixed - false
func (mix *Mix) IsMixed() bool {
	return true
}

func (mix *Mix) RotateLen() int {
	return len(mix.Games)
}

func (mix *Mix) UnmarshalJSON(data []byte) error {
	var result struct {
		Type      string
		TableSize int
	}

	err := json.Unmarshal(data, &result)

	if err != nil {
		return err
	}

	*mix = *NewMix(game.MixedGame(result.Type), result.TableSize)

	return nil
}
 * 
 */*/