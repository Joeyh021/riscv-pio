package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

class ExecUnitIO extends Bundle {}

class ExecUnit extends Module {
  val io = IO(new ExecUnitIO)
}
