package dev.joeyh.pio

import chisel3._
import chisel3.util._

//a 16 bit integer, 8 bit fractional clock divider
//based off the one in the RP2040 -- see section 3.5.5 of the datasheet
//parametrised over the system clock speed for genericity

class ClockDividerIO extends Bundle {
  val integer    = Input(UInt(16.W))
  val fractional = Input(UInt(8.W))
  //input clock is implicit
  val outputClock = Output(Clock())
}

class ClockDivider(val inputClockSpeed: Int) extends Module {
  val io = IO(new ClockDividerIO)
}

object ClockDivider {
  def apply(inputClockSpeed: Int, integer: UInt, fractional: UInt): Clock = {
    val divider = Module(new ClockDivider(inputClockSpeed))
    divider.io.integer := integer
    divider.io.fractional := fractional
    divider.io.outputClock
  }
}