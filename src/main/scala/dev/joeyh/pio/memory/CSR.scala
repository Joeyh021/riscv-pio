package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._
import dev.joeyh.pio.shiftreg.ShiftRegConfig
import dev.joeyh.pio.PinConfig

//the bank of control and status registers for the PIO block
//Uses a chisel Mem for combinational read, synchronous write
//write 16 byte words at a time (could be bad if want to write bigger chunks)
//literally just a smaller instruction memory
//parametrised so can easily add more fields

class CSRIO extends ReadWrite(UInt(32.W)) {
  val address = Input(UInt(5.W))

  val wrapTarget = Output(UInt(5.W))
  val osrCfg     = Output(new ShiftRegConfig)
  val isrCfg     = Output(new ShiftRegConfig)
  val pinCfg     = Output(new PinConfig)
  val clockDiv   = Output(UInt(32.W))
  //other config signals
  val branchPin = Output(UInt(5.W))
}

class CSR(registers: Int) extends Module {
  val io = IO(new CSRIO)

  val mem = Mem(registers, UInt(32.W))

  io.read := mem(io.address)

  when(io.write.enable) {
    mem(io.address) := io.write.data
  }

}
