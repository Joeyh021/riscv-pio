package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

//the simple 32 bit scratch register
//system has two of these, X and Y

class ScratchRegIO extends Bundle {
  val read        = Output(UInt(32.W))
  val write       = Input(UInt(32.W))
  val writeEnable = Input(Bool())
}

class ScratchReg extends Module {
  val io = IO(new ScratchRegIO)

  val reg = RegInit(0.U)
  io.read := reg
  when(io.writeEnable) {
    reg := io.write
  }
}
