# Execution Stuff

We have separate execution units for separate instructions
Move and set is one execution unit
Branch is another
Wait is another

branch and wait are another

## Move/set Unit

Inputs:

- src/dest
  - Scratch X `000`
  - Scratch Y `001`
  - ISR `010`
  - OSR `011`
  - immediate `100` (for set)
    - invalid for dest
  - null `101`
  - pins `110` - move/set use same pin mapping as out
- data
- enable
- read/write channels for all the above things

## Branch Unit (Jump)

Inputs:

- condition (3 bits)

conditions:

- `000`
  - always
- `001`
  - `!X`
  - If x zero
- `010`
  - `X--`
  - If X non-zero _prior_ to decrement
  - Decrement operation is not conditional
- `011`
  - `!Y`
  - If y zero
- `100`
  - `Y--`
  - If Y non-zero _prior_ to decrement
  - Decrement operation is not conditional
- `101`
  - `X != Y`
  - if x not equal to y
- `110`
  - If input pin configured by CSR is high
- `111`
  - `!OSRE`
  - if the output shift register is non-empty

For the increment operations

## Wait

inputs are just wait operands and pin register, outputs are if we stall or no

only one wait condition atm so very simple
