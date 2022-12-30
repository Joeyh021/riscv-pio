package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

class ProgramCounterIO extends Bundle {}

class ProgramCounter extends Module {
  val io = IO(new ProgramCounterIO)
}
