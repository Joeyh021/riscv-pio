package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

class MoveIO extends Bundle {
  val enable = Input(Bool())
  /*
  - Scratch X `000`
  - Scratch Y `001`
  - ISR `010`
  - OSR `011`
  - immediate `100` (for set)
    - invalid for dest
  - null `101`
  - pins `110` - move/set use same pin mapping as out
   */
  val src  = Input(UInt(3.W))
  val dest = Input(UInt(3.W))

  //used for set (src is immediate)
  val immediate = Input(UInt(5.W))

  //read/write for all the registers
  val x    = Flipped(ReadWrite(UInt(32.W)))
  val y    = Flipped(ReadWrite(UInt(32.W)))
  val isr  = Flipped(ReadWrite(UInt(32.W)))
  val osr  = Flipped(ReadWrite(UInt(32.W)))
  val pins = Flipped(ReadWrite(UInt(32.W)))
}

class Move extends Module {
  val io = IO(new MoveIO)

  val srcData = MuxLookup(
    io.src, //mux condition
    0.U,    //default (should not happen)
    Seq(    //cases
      0.U -> io.x.read,
      1.U -> io.y.read,
      2.U -> io.isr.read,
      3.U -> io.osr.read,
      4.U -> io.immediate.pad(32), //5 LSBs are data, MSBs all zero
      5.U -> 0.U,
      6.U -> io.pins.read.pad(32)
    )
  )

  //always write to all the outputs, but only enable the one we want
  io.x.write.data := srcData
  io.y.write.data := srcData
  io.isr.write.data := srcData
  io.osr.write.data := srcData
  io.pins.write.data := srcData

  io.x.write.enable := io.enable && io.dest === 0.U
  io.y.write.enable := io.enable && io.dest === 1.U
  io.isr.write.enable := io.enable && io.dest === 2.U
  io.osr.write.enable := io.enable && io.dest === 3.U
  //cannot write to immediate
  //if writing to null, nothing happens
  io.pins.write.enable := io.enable && io.dest === 6.U

}
