package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._

//Need to specialise behaviour for input and output shift register
sealed trait Direction
case object Input  extends Direction
case object Output extends Direction

class ShiftRegisterIO extends Bundle {}

class ShiftRegister(dir: Direction) extends Module {
  val io = IO(new ShiftRegisterIO)
}
