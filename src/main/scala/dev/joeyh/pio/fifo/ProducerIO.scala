package dev.joeyh.pio.fifo

import chisel3._
import chisel3.util._

class ProducerIO extends Bundle {
  val write   = Input(UInt(32.W))
  val doWrite = Input(Bool())
  val full    = Output(Bool())
}
