package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

class PinRegIO extends Bundle {}

class PinReg extends Module {
  val io = IO(new PinRegIO)
}
