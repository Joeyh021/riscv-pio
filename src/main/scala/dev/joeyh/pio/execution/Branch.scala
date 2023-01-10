package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._

class BranchIO extends Bundle {
  //the operation to do
  val op = Input(UInt(3.W))

  //execute on this unit
  //just enables the pc write
  val enable = Input(Bool())

  //condition inputs for jumps
  //also the write so we can increment
  val x        = Flipped(ReadWrite(UInt(32.W)))
  val y        = Flipped(ReadWrite(UInt(32.W)))
  val osrEmpty = Input(Bool())

  val pins         = Input(UInt(12.W)) //entire pin register
  val branchPinCSR = Input(UInt(5.W))  //the csr config for jump pin condition

  //write the new address to the program counter
  val address = Input(UInt(5.W))
  val PCWrite = Flipped(Write(UInt(5.W)))
}

class Branch extends Module {
  val io = IO(new BranchIO)

  val doJump = MuxLookup(
    io.op,  //mux condition
    true.B, //default (should not happen)
    Seq(    //cases
      0.U -> true.B,
      1.U -> (io.x.read === 0.U),
      2.U -> (io.x.read =/= 0.U), //also increment X
      3.U -> (io.y.read === 0.U),
      4.U -> (io.y.read =/= 0.U), //also increment Y
      5.U -> (io.x.read =/= io.y.read),
      6.U -> (io.pins(io.branchPinCSR) === true.B),
      7.U -> (io.osrEmpty === false.B)
    )
  )

  //increment conditions
  io.x.write.enable := io.op === 2.U && doJump
  io.x.write.data := io.x.read + 1.U

  io.y.write.enable := io.op === 4.U && doJump
  io.y.write.data := io.y.read + 1.U

  io.PCWrite.enable := io.enable && doJump
  io.PCWrite.data := io.address
}
