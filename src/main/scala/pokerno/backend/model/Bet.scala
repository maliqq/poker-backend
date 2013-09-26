package pokerno.backend.model
import scala.reflect.runtime.universe._

import scala.math.{BigDecimal => Decimal}


class Bet[T <: Bet.Type : TypeTag](val amount: Decimal = .0) {
  val _betType = typeOf[T]
  override def toString = {
    if (amount > .0)
      "%s %.2f".format(_betType.toString, amount)
    else
      _betType.toString
  }
}

object Bet {
  trait Type
  case class Fold extends Type
  case class Call extends Type
  case class Raise extends Type
  case class Check extends Type
  def check = new Bet[Check]()
  def fold = new Bet[Fold]()
  def call(amount: Decimal) = new Bet[Call](amount)
  def raise(amount: Decimal) = new Bet[Raise](amount)
}

/*
package model

import (
	"fmt"
)

import (
	"gopoker/model/bet"
)

// Bet - type and amount
type Bet struct {
	bet.Type
	Amount float64
}

// String - bet to string
func (b Bet) String() string {
	if b.Amount != 0. {
		return fmt.Sprintf("%s %.2f", string(b.Type), b.Amount)
	}
	return string(b.Type)
}

// PrintString - bet to print string
func (b Bet) PrintString() string {
	return b.String()
}

// IsActive - check active bet (raise or call)
func (b Bet) IsActive() bool {
	switch b.Type {
	case bet.Raise, bet.Call:
		return true
	default:
		return false
	}
}

// IsForced - check forced bet
func (b Bet) IsForced() bool {
	switch b.Type {
	case bet.Ante, bet.BringIn, bet.SmallBlind, bet.BigBlind, bet.GuestBlind, bet.Straddle:
		return true
	default:
		return false
	}
}

// NewBet - create bet
func NewBet(t bet.Type, amount float64) *Bet {
	return &Bet{Type: t, Amount: amount}
}

// NewFold - create fold
func NewFold() *Bet {
	return &Bet{Type: bet.Fold}
}

// NewRaise - create raise
func NewRaise(amount float64) *Bet {
	return &Bet{Type: bet.Raise, Amount: amount}
}

// NewCheck - create check
func NewCheck() *Bet {
	return &Bet{Type: bet.Check}
}

// NewCall - create call
func NewCall(amount float64) *Bet {
	return &Bet{Type: bet.Call, Amount: amount}
}

// Validate - validate seat bet according to bet range
func (b *Bet) Validate(seat *Seat, betRange *bet.Range) error {
	switch b.Type {
	case bet.Fold:
		// no error
	case bet.Check:
		if betRange.Call != seat.Bet {
			return fmt.Errorf("Can't check: need to call=%.2f", betRange.Call)
		}

	case bet.Call, bet.Raise:
		amount := b.Amount

		if amount > seat.Stack {
			return fmt.Errorf("Can't bet: got amount=%.2f, stack=%.2f", amount, seat.Stack)
		}

		if b.Type == bet.Call {
			return validateRange(amount, betRange.Call, betRange.Call, amount == seat.Stack)
		}

		if b.Type == bet.Raise {
			return validateRange(amount, betRange.Min, betRange.Max, amount == seat.Stack)
		}
	}

	return nil
}

func validateRange(amount float64, min float64, max float64, allIn bool) error {
	if max == 0. {
		return fmt.Errorf("Nothing to bet: got amount=%.2f", amount)
	}

	if amount > max {
		return fmt.Errorf("Bet invalid: got amount=%.2f, required max=%.2f", amount, max)
	}

	if amount < min && !allIn {
		return fmt.Errorf("Bet invalid: got amount=%.2f, required min=%.2f", amount, min)
	}

	return nil
}

}*/