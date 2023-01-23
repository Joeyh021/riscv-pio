package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._
import dev.joeyh.pio.fifo._
import dev.joeyh.pio.util.ReadWrite

class ShiftRegConfig extends Bundle {
  //the threshold before new data is read/written to/from FIFO
  val thresh      = Input(UInt(5.W)) //1-32, 0=32
  val autoEnabled = Input(Bool())

  //TRUE (1) for shift to the right, FALSE (0) for shift to the left
  val dir = Input(Bool())
}

class ShiftControl extends Bundle {
  //when this is asserted, a shift of shiftCount bits takes place
  val shift      = Input(Bool())
  val count = Input(UInt(5.W))
}

class PushPullControl extends Bundle {
  //PUSH/PULL control signals
  val doPushPull = Input(Bool())
  val iffeFlag   = Input(Bool())
}
class ShiftRegIO extends Bundle {
  val sCtl = new ShiftControl
  val pCtl = new PushPullControl
  val cfg  = new ShiftRegConfig
  val rw   = new ReadWrite(UInt(32.W))

  val shiftCountReg = Output(UInt(6.W))

  //asserted back to exec unit
  val stall = Output(Bool())

}

class ISRIO extends ShiftRegIO {
  val shiftInData = Input(UInt(32.W))
  val fifo        = Flipped(new ProducerIO)
}

class OSRIO extends ShiftRegIO {
  val shiftOutData = Output(UInt(32.W))
  val fifo         = Flipped(new ConsumerIO)
}
