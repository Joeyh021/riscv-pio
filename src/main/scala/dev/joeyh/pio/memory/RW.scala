package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

//A bundle definition used for a read/write channel in our system
//reads must be combinational, and writes are latched for the next cycle when write is valid

class RW[D <: Data](d: D) extends Bundle {
  val read = Output(d)
  val write = new Bundle {
    val enable = Input(Bool())
    val data   = Input(d)
  }

}

object RW {
  def apply[D <: Data](d: D) = new RW(d)
}
