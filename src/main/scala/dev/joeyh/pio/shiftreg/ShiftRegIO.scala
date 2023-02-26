package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._
import dev.joeyh.pio.fifo._
import dev.joeyh.pio.util._

class ShiftRegConfig extends Bundle {
  //the threshold before new data is read/written to/from FIFO
  val thresh      = UInt(5.W) //1-32, 0=32
  val autoEnabled = Bool()

  //TRUE (1) for shift to the right, FALSE (0) for shift to the left
  val dir = Bool()
}

class ShiftControl extends Bundle {
  //when this is asserted, a shift of shiftCount bits takes place
  val doShift = Bool()
  val count   = UInt(5.W)
  //PUSH/PULL control signals
  val doPushPull = Bool()
  val iffeFlag   = Bool()
}

class ShiftRegIO extends Bundle {
  val ctrl = Input(new ShiftControl)
  val cfg  = Input(new ShiftRegConfig)
  val rw   = ReadWrite(UInt(32.W))

  val shiftCountReg = Output(UInt(6.W))

  //asserted back to exec unit
  val stall = Output(Bool())

}

class ISRIO extends ShiftRegIO {
  val shiftIn = Input(UInt(32.W))
  val fifo    = Flipped(new ProducerIO)
}

class OSRIO extends ShiftRegIO {
  val shiftOut = Flipped(Write(UInt(32.W)))
  val fifo     = Flipped(new ConsumerIO)
}
