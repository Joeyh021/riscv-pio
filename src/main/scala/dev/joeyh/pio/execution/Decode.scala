package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

//instruction decode stage
//includes asserting control signals for certain instructions, or passing to other execution units if needed
class DecodeIO extends Bundle {
  val instruction = Input(UInt(16.W))

  //high if PC is to be incremented
  //used to re-execute on a stall
  val increment = Output(Bool())

  //may be asserted by control signal from fifo/shiftreg
  //may also be asserted by wait unit
  //if high, then pc not incremented and we re-exec
  val stall = Input(Bool())

  //the side set output value
  //goes straight to the pin
  val sideSet = Output(Bool())

  //control signals for shifts
  //in/out instructions assert these

  //if 0, do nothing
  val inCount  = Output(UInt(5.W))
  val outCount = Output(UInt(5.W))

  val inSrc   = Output(UInt(3.W))
  val outDest = Output(UInt(3.W))

  //control signals for push/pull
  val doPush = Output(Bool())
  val doPull = Output(Bool())
  //shared by both instructions
  val iffeFlag = Output(Bool())
  val blkFlag  = Output(Bool())

  //outputs for wait execution
  val waitPolarity = Output(Bool())
  val waitIdx      = Output(UInt(5.W))
  val waitEnable   = Output(Bool())

  //outputs for jump execution
  val branchOp      = Output(UInt(3.W))
  val branchAddress = Output(UInt(5.W))
  val branchEnable  = Output(Bool())

  //outputs for mov/set execution
  val movSrc    = Output(UInt(3.W))
  val movDest   = Output(UInt(3.W))
  val setValue  = Output(UInt(5.W))
  val movEnable = Output(Bool())

}

class Decode extends Module {
  val io = IO(new DecodeIO)

  //if an instruction includes an N cycle delay
  //then we execute NOPs (mov x, x) for the next N cycles
  val delayCycles = RegInit(0.U(4.W))
  val sleeping    = delayCycles =/= 0.U

  //decrement if sleeping as we do not wish to sleep forever
  //if we are sleeping we don't want to read the delay field of the instruction
  //if we stall, then don't delay and immediately re-exec
  delayCycles := Mux(sleeping, delayCycles - 1.U, Mux(io.stall, 0.U, io.instruction(11, 8)))

  //if we're sleeping, override instruction with `mov x,x`
  //hacky? yes. efficient? no. functional? hopefully...
  val nopInstruction = "b101_00000_111_00_111".U
  val instruction    = Mux(sleeping, nopInstruction, io.instruction)

  //increment the program counter if we're not sleeping, and instruction didn't cause a stall
  //these cannot both be high at the same time
  io.increment := !sleeping && !io.stall

  //opcode is top 3 bits
  val opcode = instruction(15, 13)

  //side set is bit 12
  io.sideSet := instruction(12)

  //implement in/out instructions
  //assert control signals for registers
  //IN
  when(opcode === 2.U) {
    io.inSrc := instruction(7, 5)
    io.inCount := instruction(4, 0)
  }.otherwise {
    io.inSrc := 0.U
    io.inCount := 0.U
  }
  //OUT
  when(opcode === 3.U) {
    io.outDest := instruction(7, 5)
    io.outCount := instruction(4, 0)
  }.otherwise {
    io.outDest := 0.U
    io.outCount := 0.U
  }

  //control signals for push/pull
  when(opcode === 4.U) {
    io.iffeFlag := instruction(6)
    io.blkFlag := instruction(5)
    when(instruction(7)) {
      io.doPull := true.B
    }.otherwise {
      io.doPush := true.B
    }
  }.otherwise {
    io.iffeFlag := false.B
    io.blkFlag := false.B
    io.doPull := false.B
    io.doPush := false.B
  }

  //jump
  when(opcode === 0.U) {
    io.branchOp := instruction(7, 5)
    io.branchAddress := instruction(4, 0)
    io.branchEnable := true.B
  }.otherwise {
    io.branchOp := 0.U
    io.branchAddress := 0.U
    io.branchEnable := false.B
  }

  //WAIT
  when(opcode === 1.U) {
    io.waitPolarity := instruction(7)
    io.waitIdx := instruction(4, 0)
    io.waitEnable := true.B
  }.otherwise {
    io.waitPolarity := 0.U
    io.waitIdx := 0.U
    io.waitEnable := false.B
  }

  //MOV/SET
  when(opcode === 5.U) {
    io.movDest := instruction(7, 5)
    io.movSrc := instruction(2, 0)
    io.setValue := 0.U
    io.movEnable := true.B
  }.elsewhen(opcode === 7.U) {
      io.movDest := instruction(7, 5)
      io.movSrc := "b100".U //immediate
      io.setValue := instruction(4, 0)
      io.movEnable := true.B
    }
    .otherwise {
      io.movDest := 0.U
      io.movSrc := 0.U
      io.setValue := 0.U
      io.movEnable := false.B
    }

}
