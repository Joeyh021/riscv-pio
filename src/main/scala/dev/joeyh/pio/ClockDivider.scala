package dev.joeyh.pio

import chisel3._
import chisel3.util._

class ClockDivider extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val divisor = Input(UInt(16.W))
    val out     = Output(Clock())
  })
  addResource("vsrc/ClockDivider.v")
}

object ClockDivider {
  def apply(divisor: UInt): Clock = {
    val clkdiv = Module(new ClockDivider)
    clkdiv.io.divisor := divisor
    clkdiv.io.out
  }
}
