package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

class MoveIO extends Bundle {}

class Move extends Module {
  val io = IO(new MoveIO)
}
