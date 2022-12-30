package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

class FetchIO extends Bundle {}

class Fetch extends Module {
  val io = IO(new FetchIO)
}
