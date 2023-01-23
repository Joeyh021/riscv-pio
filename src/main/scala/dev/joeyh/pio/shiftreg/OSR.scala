package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._
import _root_.dev.joeyh.pio.fifo.FIFOConsumerIO
import _root_.dev.joeyh.pio.util.ReadWrite

// The 32bit shift register
class OSRIO extends ReadWrite(UInt(32.W)) {
  val shiftOut = Output(UInt(32.W))
  val fifo     = Flipped(new FIFOConsumerIO)
  val ctrl     = new ShiftControl
}

class OSR extends Module {
  val io = IO(new OSRIO)
}
