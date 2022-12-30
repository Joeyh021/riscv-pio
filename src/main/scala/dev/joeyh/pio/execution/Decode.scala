package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

//instruction decode stage
//includes asserting control signals for certain instructions, or passing to ALU if needed
class DecodeIO extends Bundle {}

class Decode extends Module {
  val io = IO(new DecodeIO)
}
