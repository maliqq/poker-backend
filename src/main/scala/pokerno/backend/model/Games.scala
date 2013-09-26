package pokerno.backend.model

class Games {

}
/*
package model

import (
	"log"
)

import (
	"gopoker/model/game"
	"gopoker/poker/hand"
)

const (
	// GamesConfigFile - default games config filename
	GamesConfigFile = "games.json"
	// MixesConfigFile - default mixes config filename
	MixesConfigFile = "mixes.json"
)

// Games - game options
var Games = map[game.LimitedGame]*GameOptions{
	game.Texas: &GameOptions{
		Group:        game.Holdem,
		HasBoard:     true,
		HasBlinds:    true,
		MaxTableSize: 10,
		Hi:           hand.High,
		PocketSize:   2,
		DefaultLimit: game.NoLimit,
	},

	game.Omaha: &GameOptions{
		Group:        game.Holdem,
		HasBoard:     true,
		HasBlinds:    true,
		MaxTableSize: 10,
		PocketSize:   4,
		Hi:           hand.High,
		DefaultLimit: game.PotLimit,
	},

	game.Omaha8: &GameOptions{
		Group:        game.Holdem,
		HasBoard:     true,
		HasBlinds:    true,
		MaxTableSize: 10,
		PocketSize:   4,
		Hi:           hand.High,
		Lo:           hand.AceFive8,
		DefaultLimit: game.PotLimit,
	},

	game.Stud: &GameOptions{
		Group:        game.SevenCard,
		HasAnte:      true,
		HasBringIn:   true,
		HasVela:      true,
		MaxTableSize: 8,
		PocketSize:   7,
		Hi:           hand.High,
		DefaultLimit: game.FixedLimit,
	},

	game.Stud8: &GameOptions{
		Group:        game.SevenCard,
		HasAnte:      true,
		HasBringIn:   true,
		HasVela:      true,
		MaxTableSize: 8,
		PocketSize:   7,
		Hi:           hand.High,
		Lo:           hand.AceFive8,
		DefaultLimit: game.FixedLimit,
	},

	game.Razz: &GameOptions{
		Group:        game.SevenCard,
		HasAnte:      true,
		HasBringIn:   true,
		HasVela:      true,
		MaxTableSize: 8,
		PocketSize:   7,
		Hi:           hand.AceFive,
		DefaultLimit: game.FixedLimit,
	},

	game.London: &GameOptions{
		Group:        game.SevenCard,
		HasAnte:      true,
		HasBringIn:   true,
		HasVela:      true,
		MaxTableSize: 8,
		PocketSize:   7,
		Hi:           hand.AceSix,
		DefaultLimit: game.FixedLimit,
	},

	game.FiveCard: &GameOptions{
		Group:        game.SingleDraw,
		HasBlinds:    true,
		Discards:     true,
		Reshuffle:    true,
		MaxTableSize: 6,
		PocketSize:   5,
		Streets:      1,
		Hi:           hand.High,
		DefaultLimit: game.FixedLimit,
	},

	game.Single27: &GameOptions{
		Group:        game.SingleDraw,
		HasBlinds:    true,
		Discards:     true,
		Reshuffle:    true,
		MaxTableSize: 6,
		PocketSize:   5,
		Streets:      1,
		Hi:           hand.DeuceSeven,
		DefaultLimit: game.FixedLimit,
	},

	game.Triple27: &GameOptions{
		Group:        game.TripleDraw,
		HasBlinds:    true,
		Discards:     true,
		Reshuffle:    true,
		MaxTableSize: 6,
		PocketSize:   5,
		Streets:      3,
		Hi:           hand.DeuceSeven,
		DefaultLimit: game.FixedLimit,
	},

	game.Badugi: &GameOptions{
		Group:        game.TripleDraw,
		HasBlinds:    true,
		Discards:     true,
		Reshuffle:    true,
		MaxTableSize: 6,
		PocketSize:   4,
		Hi:           hand.Badugi,
		DefaultLimit: game.FixedLimit,
	},
}

// Mixes - mix options
var Mixes = map[game.MixedGame][]MixOptions{
	game.Horse: []MixOptions{
		MixOptions{
			Type:  game.Texas,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Omaha8,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Razz,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Stud,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Stud8,
			Limit: game.FixedLimit,
		},
	},

	game.Eight: []MixOptions{
		MixOptions{
			Type:  game.Triple27,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Texas,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Omaha8,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Razz,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Stud,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Stud8,
			Limit: game.FixedLimit,
		},
		MixOptions{
			Type:  game.Texas,
			Limit: game.NoLimit,
		},
		MixOptions{
			Type:  game.Omaha,
			Limit: game.PotLimit,
		},
	},
}

// LoadGames - load games from config dir
func LoadGames(configDir string) {
	ReadConfig(configDir, GamesConfigFile, &Games)
	log.Printf("[config] games loaded: %d", len(Games))

	ReadConfig(configDir, MixesConfigFile, &Mixes)
	log.Printf("[config] mixes loaded: %d", len(Mixes))
}
*/