package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

//the simple 32 bit scratch register
//system has two of these, X and Y

class ScratchReg extends Module {
  val io = IO(new RWBundle)

  val reg = RegInit(0.U)
  io.read := reg
  when(io.write.valid) {
    reg := io.write.bits
  }
}
