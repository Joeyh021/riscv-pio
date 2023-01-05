# Instruction Set

The primary reference is [section 3 of the RP2040 datasheet](https://datasheets.raspberrypi.com/rp2040/rp2040-datasheet.pdf). The instruction set for these pio blocks is adapted from those, with some simplifications. See also `Decode.scala`.

## Primary Differences from RP2040

- There is no IRQ instruction
- delay/side set not configurable
  - bit 12 always side set pin
  - bits 11-8 always delay

## Instruction Format

- Bits 15-13 are the opcode
  - Same opcodes as rp2040
  - no IRQ instruction
- Bit 12 is the side set
- Bits 11-8 are the delay amount

## Instructions

### JMP

set PC to immediate operand if condition is true

- always
- if x/y zero/nonzero/equal
- on input pin

### WAIT

stall until condition met. can set polarity

- wait on input pin to meet a condition
- Bit 7 is polarity
- bits 6-5 unused
- Bits 4-0 select pin

### IN

shift data in (N bits)

- Bits 7-5 is source
  - 000 - pins
  - 001 - X
  - 010 - Y
  - 011 - Zeros
  - 110 - ISR
  - 111 - OSR
- Bits 4-0 is count

- pins
- x
- y
- null (zeros)

increase in shift count, saturate at 32

if autopush enabled, then when shift count == thresh, push and reset

### OUT

shift data out (N bits)

- pins
- x
- y
- null

writes 32 bits to dest, N lsb from reg, rest 0s

increase out shift count (saturate at 32)

if autopull enabled, then when shift count == thresh, reset and pull

### PUSH

store contents of isr to rx fifo. clear isr to all zeros.

- if iff flag set, then only do if isr full
- if blk flag set, stall if fifo full

### PULL

load 32-bit word from tx fifo to OSR

- if ife flag set, do nothing unless osr empty
- if blk flag set, stall if fifo empty
  - if not set, pulling from empty pulls from scratch X

### MOV

Copy data from src to dest

may do an operation (none, flip, reverse)

- pins
- x
- y
- isr
- osr
- null

### SET

Write an immediate value to a destination

- 000 - pins
- 001 - X
- 010 - Y

## Other Details

### side-set

sets pin 0 at the same time the instruction executes

### delay

Insert N NOPs after the instruction

Only delay _after_ a stalled instruction executes successfully

### autopull/push

when the osr is empty and 32 bits have been shifted, can use autopull to auto refill from fifo

same for isr

### stalls

if we stall, then we don't move the program counter and try to re-exec on the next cycle
