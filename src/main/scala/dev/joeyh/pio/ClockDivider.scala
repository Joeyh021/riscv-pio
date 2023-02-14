package dev.joeyh.pio

import chisel3._
import chisel3.util._

class ClockDivider extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock   = Input(Clock())
    val divisor = Input(UInt(16.W))
    val outClk  = Output(Clock())
  })
  addResource("vsrc/ClockDivider.v")
}

object ClockDivider {
  def apply(divisor: UInt, clock: Clock, reset: Bool): Clock = {
    val clkdiv = Module(new ClockDivider)
    clkdiv.io.clock := clock
    clkdiv.io.divisor := divisor
    clkdiv.io.outClk
  }
}
