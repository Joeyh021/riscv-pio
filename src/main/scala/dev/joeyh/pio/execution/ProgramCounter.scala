package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

class WrapConfig extends Bundle {
  val enable  = Bool()
  val target  = UInt(5.W)
  val trigger = UInt(5.W)
}

//the system program counter
//5 bits - only 32 instructions max
class ProgramCounterIO extends ReadWrite(UInt(5.W)) {
  // if non-zero, the program counter will wrap early
  val wrapCfg = Input(new WrapConfig)

  //if high, the counter is incremented for the next cycle
  //used for delays/stalls
  val increment = Input(Bool())
}

class ProgramCounter extends Module {
  val io = IO(new ProgramCounterIO)

  val reg = RegInit(0.U)
  io.read := reg

  when(io.write.enable) {
    //ignore increment if trying to write
    //ignore wrap when writing directly
    reg := io.write.data
  }.elsewhen(io.increment && !io.write.enable) {
    //only increment if no writeEn
    //wrap if we need to
    when(io.wrapCfg.enable && reg === io.wrapCfg.trigger) {
      reg := io.wrapCfg.target
    } otherwise {
      // will wrap around 5 bits anyways
      reg := reg + 1.U
    }
  }

}
