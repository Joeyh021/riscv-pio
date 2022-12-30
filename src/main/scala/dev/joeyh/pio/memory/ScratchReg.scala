package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

class ScratchRegIO extends Bundle {}

class ScratchReg extends Module {
  val io = IO(new ScratchRegIO)
}
