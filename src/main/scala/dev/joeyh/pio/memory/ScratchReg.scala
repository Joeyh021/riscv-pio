package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

//the simple 32 bit scratch register
//system has two of these, X and Y

class ScratchReg extends Module {
  val io = IO(ReadWrite(UInt(32.W)))

  val reg = RegInit(0.U)
  io.read := reg
  when(io.write.enable) {
    reg := io.write.data
  }
}
