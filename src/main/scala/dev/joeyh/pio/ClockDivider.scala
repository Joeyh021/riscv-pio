package dev.joeyh.pio

import chisel3._
import chisel3.util._

class ClockDividerIO extends Bundle {}

class ClockDivider extends Module {
  val io = IO(new ClockDividerIO)
}
