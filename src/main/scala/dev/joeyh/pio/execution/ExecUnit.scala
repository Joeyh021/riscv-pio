package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

//top level execution unit
//connects to instruction memory, has data ins/outs for rest of system
class ExecUnitIO extends Bundle {}

class ExecUnit extends Module {
  val io = IO(new ExecUnitIO)
}
