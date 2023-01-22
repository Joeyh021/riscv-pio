package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

//the pin mapping register
//maps directly to the GPIO pins
//reads directions and enables from csr
class PinRegIO extends ReadWrite(UInt(32.W)) {
  val pinConfigs = Input(UInt(32.W))

  //this needs wrapping in a verilog module
  //because chisel doesn't support inout
  val inputMapping  = Input(UInt(32.W))
  val outputMapping = Output(UInt(32.W))
}

class PinReg extends Module {
  val io = IO(new PinRegIO)

  val inBase   = io.pinConfigs(7, 0)
  val inCount  = io.pinConfigs(15, 8)
  val outBase  = io.pinConfigs(23, 16)
  val outCount = io.pinConfigs(31, 24)

  val inMask  = ((1.U << inCount) - 1.U)
  val outMask = ((1.U << outCount) - 1.U)

  val reg = RegInit(0.U(32.W))
  io.read := (reg >> inBase) & inMask

  when(io.write.enable) {
    reg := (io.write.data >> outBase) & outMask
  }

}
