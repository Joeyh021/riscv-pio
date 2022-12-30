package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._

//Need to specialise behaviour for input and output shift register
sealed trait Direction
case object ShiftIn  extends Direction
case object ShiftOut extends Direction

// The 32bit shift register

class ShiftRegisterIO(val dir: Direction) extends Bundle {
  //read/write a full 32 bit word to the register
  val readWord  = Output(UInt(32.W))
  val writeWord = Input(UInt(32.W))

  //resets the shift count to 0
  val countRst = Input(Bool())

  //the threshold before new data is read from FIFO
  val autoThresh = Input(UInt(5.W))

  //when this is asserted, a shift of shiftCount bits takes place
  val shift      = Input(Bool())
  val shiftCount = Input(UInt(5.W))

  //when shift, the shifted bits are presented at this port as the N least significant bits
  val shiftOut = if (dir == ShiftOut) Some(Output(UInt(32.W))) else None

  //when shift, the N least significant bits are read from this port
  val shiftIn = if (dir == ShiftIn) Some(Input(UInt(32.W))) else None

}

class ShiftRegister(val dir: Direction) extends Module {
  val io = IO(new ShiftRegisterIO(dir))
}
