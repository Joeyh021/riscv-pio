package blinky

import chisel3._
import chisel3.util._

class Blink(clockSpeed: Int = 100_000_000, speed: Int = 1) extends Module {
  val io = IO(new Bundle {
    val led0   = Output(Bool())
    val enable = Input(Bool())
  })

  val led = RegInit(true.B)
  io.led0 := led

  val (_, counterWrap) = Counter(true.B, clockSpeed / speed)
  when(counterWrap && io.enable) {
    led := ~led
  }
}
