package dev.joeyh.pio.fifo

import chisel3._
import chisel3.util._

class FIFOConsumerIO extends Bundle {
  val read   = Output(UInt(32.W))
  val doRead = Input(Bool())
  val empty  = Output(Bool())
}
