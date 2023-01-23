package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._

class ShiftControl extends Bundle {
  //from control unit

  //when this is asserted, a shift of shiftCount bits takes place
  val shift      = Input(Bool())
  val shiftCount = Input(UInt(5.W))

  //PUSH/PULL control signals
  val doPushPull = Input(Bool())
  val iffeFlag   = Input(Bool())

  //asserted back to decode unit
  val stall = Output(Bool())

  //from CSR

  //the threshold before new data is read/written to/from FIFO
  val pushPullThresh      = Input(UInt(5.W)) //1-32, 0=32
  val autoPushPullEnabled = Input(Bool())

  //TRUE (1) for shift to the right, FALSE (0) for shift to the left
  val shiftDir = Input(Bool())

}
