package dev.joeyh.pio

import chisel3._
import chisel3.util._

class PIOIO extends Bundle {}

class PIO extends Module {
  val io = IO(new PIOIO)
}
