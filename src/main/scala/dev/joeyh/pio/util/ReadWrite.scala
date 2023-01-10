package dev.joeyh.pio.util

import chisel3._
import chisel3.util._

//A bundle definition used for a read/write channel in our system
//reads must be combinational, and writes are latched for the next cycle when write is valid

class Write[D <: Data](d: D) extends Bundle {
  val enable = Input(Bool())
  val data   = Input(d)
}

class ReadWrite[D <: Data](d: D) extends Bundle {
  val read  = Output(d)
  val write = new Write(d)

}

object Write {
  def apply[D <: Data](d: D) = new Write(d)
}

object ReadWrite {
  def apply[D <: Data](d: D) = new ReadWrite(d)
}
