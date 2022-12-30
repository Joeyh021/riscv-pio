package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

//the system program counter
//5 bits - only 32 instructions max
class ProgramCounterIO extends Bundle {
  val read = Output(UInt(5.W))

  // if non-zero, the program counter will wrap early
  val wrapTarget = Input(UInt(5.W))

  //used for set/jumps
  val write = Input(UInt(5.W))

  //if high, the counter is incremented for the next cycle
  //used for delays/stalls
  val increment = Input(Bool())
}

class ProgramCounter extends Module {
  val io = IO(new ProgramCounterIO)

}
