package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._
import dev.joeyh.pio.memory.RWBundle

//the system program counter
//5 bits - only 32 instructions max
class ProgramCounterIO extends Bundle {
  val rw = new RWBundle()

  // if non-zero, the program counter will wrap early
  val wrapTarget = Input(UInt(5.W))

  //used for set/jumps
  val write = Input(Valid(UInt(5.W)))

  //if high, the counter is incremented for the next cycle
  //used for delays/stalls
  val increment = Input(Bool())
}

class ProgramCounter extends Module {
  val io = IO(new ProgramCounterIO)

  val reg = RegInit(0.U)
  io.rw.read := reg

  when(io.write.valid) {
    //ignore increment if trying to write
    //ignore wrap when writing directly
    reg := io.write.bits
  }.elsewhen(io.increment && !io.write.valid) {
    //only increment if no writeEn
    //wrap if we need to
    when(io.wrapTarget =/= 0.U && reg === io.wrapTarget) {
      reg := 0.U
    } otherwise {
      // will wrap around 5 bits anyways
      reg := reg + 1.U
    }
  }

}
