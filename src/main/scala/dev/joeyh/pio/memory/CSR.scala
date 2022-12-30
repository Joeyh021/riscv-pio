package dev.joeyh.pio.memory

import chisel3._
import chisel3.util._

//the bank of control and status registers for the PIO block
//Uses a chisel Mem for combinational read, synchronous write

// address map:
// 0x00 - clock divider config | 16 int | 8 frac | 8 unused
// 0x04 - autopush/pull thresh | 5 push | 3 unused | 5 pull | 3 unused
// 0x06 - wrap target
// 0x08 pin dirs
// 0x09 pin enables

class CSRIO extends Bundle {}

class CSR extends Module {
  val io = IO(new CSRIO)
}
