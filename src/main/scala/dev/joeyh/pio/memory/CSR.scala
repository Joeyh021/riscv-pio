package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._
import dev.joeyh.pio.shiftreg.ShiftRegConfig
import dev.joeyh.pio.PinConfig
import chisel3.experimental.dataview.DataView

//the bank of control and status registers for the PIO block
//Uses a chisel Mem for combinational read, synchronous write
//a bank of 6 registers

class CSRIO extends ReadWrite(UInt(16.W)) {
  val address = Input(UInt(3.W))

  val clockDiv   = Output(UInt(16.W))
  val branchPin  = Output(UInt(5.W))
  val wrapTarget = Output(UInt(5.W))

  val pinCfg = Output(new PinConfig)
  val isrCfg = Output(new ShiftRegConfig)
  val osrCfg = Output(new ShiftRegConfig)
}

class CSR extends Module {
  val io = IO(new CSRIO)

  val addressMap = Map(
    "clockDiv"   -> 0x0,
    "branchPin"  -> 0x2,
    "wrapTarget" -> 0x4,
    "pinCfg"     -> 0x6,
    "isrCfg"     -> 0x8,
    "osrCfg"     -> 0xA
  )

  val mem = Mem(6, UInt(32.W))

  io.read := mem.read(io.address)

  when(io.write.enable) {
    mem.write(io.address, io.write.data)
  }

  //reads for individual registers
  io.clockDiv := mem(0)

  io.branchPin := mem(1)(4, 0)
  io.wrapTarget := mem(2)(4, 0)

  io.pinCfg := mem(3).asTypeOf(new PinConfig)

  io.osrCfg.autoEnabled := mem(4)(0) //could use dataview implicits here but probably unnecessary
  io.osrCfg.dir := mem(4)(1)
  io.osrCfg.thresh := mem(4)(6, 2)

  io.isrCfg.autoEnabled := mem(5)(0)
  io.isrCfg.dir := mem(5)(1)
  io.isrCfg.thresh := mem(5)(6, 2)

}
