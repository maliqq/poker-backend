package pokerno.backend.model

class Pot {

}/*package model

// Pot - pot
type Pot struct {
	Main *SidePot
	Side []*SidePot
}

// NewPot - create new pot
func NewPot() *Pot {
	return &Pot{
		Main: NewSidePot(0.),
		Side: []*SidePot{},
	}
}

// String - pot to string
func (pot *Pot) String() string {
	s := "\n"
	for _, pot := range pot.SidePots() {
		s += pot.String()
	}
	return s
}

// Total - get pot total
func (pot *Pot) Total() float64 {
	sum := 0.
	for _, sidePot := range pot.SidePots() {
		sum += sidePot.Total()
	}

	return sum
}

// SidePots - all side pots
func (pot *Pot) SidePots() []*SidePot {
	//return append(pot.Side, pot.Main)
	pots := []*SidePot{}

	if pot.Main.IsActive() {
		pots = append(pots, pot.Main)
	}

	for _, side := range pot.Side {
		if side.IsActive() {
			pots = append(pots, side)
		}
	}

	return pots
}

// Split - split pot by barrier
func (pot *Pot) Split(member Player, amount float64) {
	main, side := pot.Main.Split(member, amount)
	pot.Side = append(pot.Side, side)
	pot.Main = main
}

// Add - add amount to pot
func (pot *Pot) Add(member Player, amount float64, allin bool) {
	remain := amount
	for _, side := range pot.Side {
		remain = side.Add(member, remain)
	}

	if allin {
		pot.Split(member, remain)
	} else {
		pot.Main.Add(member, remain)
	}
}
* 
*/*/