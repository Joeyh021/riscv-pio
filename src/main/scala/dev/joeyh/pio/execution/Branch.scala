package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

class BranchUnitIO extends Bundle {
  //the operation to do
  val op = Input(UInt(3.W))

  //execute on this unit
  //just enables the pc write
  val enable = Input(Bool())

  //condition inputs for jumps
  val X        = Input(UInt(32.W))
  val Y        = Input(UInt(32.W))
  val osrEmpty = Input(Bool())

  val pins         = Input(UInt(12.W)) //entire pin register
  val branchPinCSR = Input(UInt(5.W))  //the csr config for jump pin condition

  //write the new address to the program counter
  val address = Input(UInt(5.W))
  val PCWrite = Output(Valid(UInt(5.W)))

}

class BranchUnit extends Module {
  val io = IO(new BranchUnitIO)

  val doJump = MuxLookup(
    io.op,  //mux condition
    true.B, //default (should not happen)
    Seq(    //cases
      0.U -> true.B,
      1.U -> (io.X === 0.U),
      2.U -> (io.X =/= 0.U),
      3.U -> (io.Y === 0.U),
      4.U -> (io.Y =/= 0.U),
      5.U -> (io.X =/= io.Y),
      6.U -> (io.pins(io.branchPinCSR) === true.B),
      7.U -> (io.osrEmpty === false.B)
    )
  )

  io.PCWrite.valid := io.enable
  io.PCWrite.bits := io.address
}
