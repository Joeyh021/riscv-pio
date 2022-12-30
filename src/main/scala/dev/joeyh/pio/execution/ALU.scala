package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

class ALUIO extends Bundle {}

class ALU extends Module {
  val io = IO(new ALUIO)
}
