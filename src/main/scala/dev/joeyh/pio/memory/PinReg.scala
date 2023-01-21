package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

//the pin mapping register
//maps directly to the GPIO pins
//reads directions and enables from csr
class PinRegIO extends ReadWrite(UInt(32.W)) {}

class PinReg extends Module {
  val io = IO(new PinRegIO)
}
