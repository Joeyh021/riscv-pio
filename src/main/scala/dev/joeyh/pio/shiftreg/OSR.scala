package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._
import _root_.dev.joeyh.pio.fifo.ConsumerIO
import _root_.dev.joeyh.pio.util.ReadWrite

class OSR extends Module {
  val io = IO(new OSRIO)
}
