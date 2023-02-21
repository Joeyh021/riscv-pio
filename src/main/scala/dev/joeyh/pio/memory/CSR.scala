package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._
import dev.joeyh.pio.shiftreg.ShiftRegConfig
import dev.joeyh.pio.PinConfig
import chisel3.experimental.dataview.DataView
import dev.joeyh.pio.execution.WrapConfig

//the bank of control and status registers for the PIO block
//Uses a chisel Mem for combinational read, synchronous write
//a bank of 6 registers

class CSRIO extends ReadWrite(UInt(16.W)) {
  val address = Input(UInt(3.W))

  val clockDiv  = Output(UInt(16.W))
  val branchPin = Output(UInt(5.W))

  val wrapCfg = Output(new WrapConfig)
  val pinCfg  = Output(new PinConfig)
  val isrCfg  = Output(new ShiftRegConfig)
  val osrCfg  = Output(new ShiftRegConfig)

  val pioEnable = Output(Bool())
}

class CSR extends Module {
  val io = IO(new CSRIO)
  object addressMap {
    val clockDiv        = 0.U
    val branchPin       = 1.U
    val wrapConfig      = 2.U
    val inputPinConfig  = 3.U
    val outputPinConfig = 4.U
    val isrCfg          = 5.U
    val osrCfg          = 6.U
    val enable          = 7.U
  }

  val mem = Mem(8, UInt(16.W))

  io.read := mem.read(io.address)

  when(io.write.enable) {
    mem.write(io.address, io.write.data)
  }

  //reads for individual registers
  io.clockDiv := mem(addressMap.clockDiv)

  io.branchPin := mem(addressMap.branchPin)(4, 0)

  io.wrapCfg.enable := mem(addressMap.wrapConfig)(15)
  io.wrapCfg.target := mem(addressMap.wrapConfig)(4, 0)
  io.wrapCfg.trigger := mem(addressMap.wrapConfig)(9, 5)

  io.pinCfg.inBase := mem(addressMap.inputPinConfig)(7, 0)
  io.pinCfg.inCount := mem(addressMap.inputPinConfig)(15, 8)
  io.pinCfg.outBase := mem(addressMap.outputPinConfig)(7, 0)
  io.pinCfg.outCount := mem(addressMap.outputPinConfig)(15, 8)

  io.osrCfg.autoEnabled := mem(addressMap.osrCfg)(0) //could use dataview implicits here but probably unnecessary
  io.osrCfg.dir := mem(addressMap.osrCfg)(1)
  io.osrCfg.thresh := mem(addressMap.osrCfg)(6, 2)

  io.isrCfg.autoEnabled := mem(addressMap.isrCfg)(0)
  io.isrCfg.dir := mem(addressMap.isrCfg)(1)
  io.isrCfg.thresh := mem(addressMap.isrCfg)(6, 2)

  io.pioEnable := mem(addressMap.enable) =/= 0.U

}
