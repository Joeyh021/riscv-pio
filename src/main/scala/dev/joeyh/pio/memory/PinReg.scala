package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

class PinRegConfig extends Bundle {
  val inBase   = UInt(8.W)
  val inCount  = UInt(8.W)
  val outBase  = UInt(8.W)
  val outCount = UInt(8.W)
}

//the pin mapping register
//maps directly to the GPIO pins
//reads directions and enables from csr
class PinRegIO extends ReadWrite(UInt(32.W)) {
  val cfg = Input(new PinRegConfig)

  //this needs wrapping in a verilog module
  //because chisel doesn't support inout
  val inputMapping  = Input(UInt(32.W))
  val outputMapping = Output(UInt(32.W))
}

class PinReg extends Module {
  val io = IO(new PinRegIO)

  val inMask  = ((1.U << io.cfg.inCount) - 1.U)
  val outMask = ((1.U << io.cfg.outCount) - 1.U)

  val reg = RegInit(0.U(32.W))
  io.read := (reg >> io.cfg.inBase) & inMask

  when(io.write.enable) {
    reg := (io.write.data >> io.cfg.outBase) & outMask
  }

}
