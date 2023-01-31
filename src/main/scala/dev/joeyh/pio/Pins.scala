package dev.joeyh.pio

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._
import chisel3.experimental.Analog

class PinConfig extends Bundle {
  val inBase   = UInt(8.W)
  val inCount  = UInt(8.W)
  val outBase  = UInt(8.W)
  val outCount = UInt(8.W)
}

//the pin mapping register
//maps directly to the GPIO pins
//reads directions and enables from csr

//this needs wrapping in a verilog module
//because chisel doesn't support inout
class PinIO extends ReadWrite(UInt(32.W)) {
  val cfg  = Input(new PinConfig)
  val pins = Analog(32.W)

}

class Pins extends BlackBox with HasBlackBoxResource {
  val io = IO(new PinIO)
  addResource("vsrc/Pins.v")
}
