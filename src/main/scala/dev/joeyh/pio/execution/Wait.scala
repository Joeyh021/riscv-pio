package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

//computes wait conditions and asserts if needed
class WaitIO extends Bundle {
  //wait on a 1 or a 0
  val polarity = Input(Bool())

  val pins   = Input(UInt(32.W))
  val pinIdx = Input(UInt(5.W))

  val enable = Input(Bool())

  val doStall = Output(Bool())
}

class Wait extends Module {
  val io = IO(new WaitIO)

  //stall if the pin is not the right polarity
  io.doStall := io.enable && io.pins(io.pinIdx) =/= io.polarity

}
