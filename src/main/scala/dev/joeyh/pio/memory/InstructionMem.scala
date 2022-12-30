package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

//32x16bit instruction memory

class InstructionMemIO extends Bundle {}

class InstructionMem extends Module {
  val io = IO(new InstructionMemIO)

  val mem = Mem(32, UInt(16.W))
}
