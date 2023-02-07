package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

//32x16bit instruction memory

class InstructionMemIO extends ReadWrite(UInt(16.W)) {
  val readAddress  = Input(UInt(5.W))
  val writeAddress = Input(UInt(5.W))
}

class InstructionMem extends Module {
  val io = IO(new InstructionMemIO)

  val mem = Mem(32, UInt(16.W))

  io.read := mem.read(io.readAddress)

  when(io.write.enable) {
    mem.write(io.writeAddress, io.write.data)
  }

}
