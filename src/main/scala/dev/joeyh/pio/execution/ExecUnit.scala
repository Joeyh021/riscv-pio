package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util._
import dev.joeyh.pio.shiftreg._

//top level execution unit
//connects to instruction memory, has data ins/outs for system registers
class ExecUnitIO extends Bundle {
  //read from instruction memory
  val instruction        = Input(UInt(16.W))
  val instructionAddress = Output(UInt(5.W))

  //wrap target config from CSR from PC
  val wrapCfg = Input(new WrapConfig)

  //register reads/writes
  val x    = Flipped(ReadWrite(UInt(32.W)))
  val y    = Flipped(ReadWrite(UInt(32.W)))
  val osr  = Flipped(ReadWrite(UInt(32.W)))
  val isr  = Flipped(ReadWrite(UInt(32.W)))
  val pins = Flipped(ReadWrite(UInt(32.W)))

  val sideSet = Output(Bool())

  //control signals for shift registers
  val isrCtl  = Output(new ShiftControl)
  val isrDest = Output(UInt(2.W))
  val osrCtl  = Output(new ShiftControl)
  val osrDest = Output(UInt(2.W))

  //fifos or shift registers may cause a stall
  val stall = Input(Bool())

  //control signal for when osr is empty
  val osrEmpty = Input(Bool())

  //the csr config for jump pin condition
  val branchPin = Input(UInt(5.W))

}

class ExecUnit extends Module {
  val io = IO(new ExecUnitIO)

  //connect up the program counter
  val pc = Module(new ProgramCounter)
  pc.io.wrapCfg := io.wrapCfg

  //address for instruction read comes from pc
  io.instructionAddress := pc.io.read

  //instruction decoding
  val decode = Module(new Decode)
  //decode read from instruction memory
  decode.io.instruction := io.instruction

  //other decode signals
  pc.io.increment := decode.io.increment
  io.sideSet := decode.io.sideSet

  //connect control for shiftreg
  io.isrCtl := decode.io.isrCtl
  io.isrDest := decode.io.isrSrc
  io.osrCtl := decode.io.osrCtl
  io.osrDest := decode.io.osrDest

  //branching unit
  val branch = Module(new Branch)
  branch.io.enable := decode.io.branchEnable
  branch.io.op := decode.io.branchOp
  branch.io.address := decode.io.branchAddress
  branch.io.osrEmpty := io.osrEmpty
  branch.io.branchPin := io.branchPin
  branch.io.x.read := io.x.read
  branch.io.y.read := io.y.read
  branch.io.pins := io.pins.read

  //branch unit can write to the PC
  //PC will ignore increment if write is enabled
  pc.io.write := branch.io.pcWrite

  //wait unit
  val waitUnit = Module(new Wait)
  waitUnit.io.enable := decode.io.waitEnable
  waitUnit.io.polarity := decode.io.waitPolarity
  waitUnit.io.pinIdx := decode.io.waitIdx
  waitUnit.io.pins := io.pins.read

  //move unit
  val move = Module(new Move)
  move.io.enable := decode.io.movEnable
  move.io.src := decode.io.movSrc
  move.io.dest := decode.io.movDest
  move.io.immediate := decode.io.setValue
  move.io.x.read := io.x.read
  move.io.y.read := io.y.read

  //only the move unit writes to shiftreg or pins
  //bi-directioncal connection! magic!
  io.osr <> move.io.osr
  io.isr <> move.io.isr
  io.pins <> move.io.pins

  //stall may come from wait unit or external
  decode.io.stall := io.stall | waitUnit.io.doStall

  //scratch register output connections
  //or the write enables
  io.x.write.enable := move.io.x.write.enable | branch.io.x.write.enable
  io.y.write.enable := move.io.y.write.enable | branch.io.y.write.enable

  io.x.write.data := move.io.x.write.data | branch.io.x.write.data
  io.y.write.data := move.io.y.write.data | branch.io.y.write.data
  //mux the data write between move/branch, if neither enabled than output 0
  io.x.write.data := MuxCase(
    0.U,
    Seq(
      move.io.x.write.enable   -> move.io.x.write.data,
      branch.io.x.write.enable -> branch.io.x.write.data
    )
  )
  io.y.write.data := MuxCase(
    0.U,
    Seq(
      move.io.y.write.enable   -> move.io.y.write.data,
      branch.io.y.write.enable -> branch.io.y.write.data
    )
  )

}
