package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

class CSRIO extends Bundle {}

class CSR extends Module {
  val io = IO(new CSRIO)
}
