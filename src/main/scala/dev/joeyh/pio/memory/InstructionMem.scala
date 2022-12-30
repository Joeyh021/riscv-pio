package dev.joeyh.pio.memory


import chisel3._
import chisel3.util._

class InstructionMemIO extends Bundle {}

class InstructionMem extends Module {
  val io = IO(new InstructionMemIO)
}
