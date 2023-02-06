package dev.joeyh.pio

import chisel3._
import chisel3.util._

//A clock divider that divides the input clock by a constant integer divisor

class ClockDivider extends Module {
  val io = IO(new Bundle {
    val divisor = Input(UInt(16.W))
    val out     = Output(Clock())
  })
  val reg = RegInit(0.U(16.W))

  when(reg === io.divisor - 1.U) {
    reg := 0.U
    io.out := true.B.asClock
  }.otherwise {
    reg := reg + 1.U
    io.out := false.B.asClock
  }

}

object ClockDivider {
  def apply(divisor: UInt): Clock = {
    val clkdiv = Module(new ClockDivider)
    clkdiv.io.divisor := divisor
    clkdiv.io.out
  }
}
