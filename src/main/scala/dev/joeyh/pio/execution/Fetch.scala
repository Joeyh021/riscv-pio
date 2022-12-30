package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

//the fetch stage of the instruction pipeline
//includes program counter and dealing with stalls/waits
class FetchIO extends Bundle {}

class Fetch extends Module {
  val io = IO(new FetchIO)
}
