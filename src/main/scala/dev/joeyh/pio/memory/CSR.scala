package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

//the bank of control and status registers for the PIO block
//Uses a chisel Mem for combinational read, synchronous write
//write 16 byte words at a time (could be bad if want to write bigger chunks)
//literally just a smaller instruction memory
//parametrised so can easily add more fields

//address map
object CSRAddresses {
  val clockDividerInteger    = 0x00 //entire 16 bits is clock integer clock divisor
  val clockDividerFractional = 0x02 //8 lower bits is fraction (byte 0x03 unused)
  val autopushPull           = 0x04 //0x04 lower 5 is push, 0x05 lower 5 is pull
  val wrapTarget             = 0x06 //lower 5 bits is wrap target
  val pinConfigs             = 0xA  //1 byte each: in base, in count, out base, out count
}

class CSRIO extends ReadWrite(UInt(16.W)) {
  val address = Input(UInt(5.W))
}

class CSR(registers: Int) extends Module {
  val io = IO(new CSRIO)

  val mem = Mem(registers, UInt(16.W))

  io.read := mem(io.address)

  when(io.write.enable) {
    mem(io.address) := io.write.data
  }
}
