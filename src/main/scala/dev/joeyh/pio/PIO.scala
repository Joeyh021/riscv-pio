package dev.joeyh.pio

import chisel3._
import chisel3.util._

//the top level PIO interface
class PIOIO extends Bundle {}

//top level PIO module
//most of the subcomponents are connected in here
class PIO extends Module {
  val io = IO(new PIOIO)

  val clockDiv = Module(new ClockDivider(100_000_000))

  withClock(clockDiv.io.outputClock) {

    val execUnit = Module(new execution.ExecUnit)

    val isr = Module(new shiftreg.ISR)
    val osr = Module(new shiftreg.OSR)
  }

}
