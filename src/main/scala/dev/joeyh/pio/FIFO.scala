package dev.joeyh.pio

import chisel3._
import chisel3.util._

//Need to specialise behaviour for input and output fifo
sealed trait Direction
case object Input  extends Direction
case object Output extends Direction

class FIFOIO extends Bundle {}

class FIFO(dir: Direction) extends Module {

  val io = IO(new FIFOIO)
}
