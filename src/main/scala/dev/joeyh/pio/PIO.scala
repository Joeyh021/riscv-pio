package dev.joeyh.pio

import chisel3._
import chisel3.util._

//the top level PIO interface
class PIOIO extends Bundle {}

//top level PIO module
//most of the subcomponents are connected in here
class PIO extends Module {
  val io = IO(new PIOIO)
}
