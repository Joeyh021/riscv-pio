package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

//32x16bit instruction memory

class InstructionMemIO extends ReadWrite(UInt(16.W)) {
  val address = Input(UInt(5.W))
}

class InstructionMem extends Module {
  val io = IO(new InstructionMemIO)

  val mem = Mem(32, UInt(16.W))

  io.read := mem(io.address)

  when(io.write.enable) {
    mem(io.address) := io.write.data
  }

}
