package dev.joeyh.pio

import chisel3._
import chisel3.util._

//An asynchronous 4x32bit FIFO
//allows for read/writing in different clock domains
//can shift in either direction, parametrised at build time

//IDEA: Potentially use chisel.util.Decoupled and util.Queue here

class FIFOIO extends Bundle {
  val read  = Output(UInt(32.W))
  val write = Input(UInt(32.W))
  val empty = Output(Bool())
  val full  = Output(Bool())
}

class FIFO() extends Module {

  val io = IO(new FIFOIO)
}
