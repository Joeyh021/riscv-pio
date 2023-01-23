package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._
import dev.joeyh.pio.shiftreg._

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

  //control signals for shift registers
  val isrCtl  = Output(new ShiftControl)
  val isrSrc  = Output(UInt(2.W))
  val osrCtl  = Output(new ShiftControl)
  val osrDest = Output(UInt(2.W))

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

  //sleeping if we have a delay did not stall
  //delays execute after stalls
  val sleeping = delayCycles =/= 0.U

  //decrement if sleeping as we do not wish to sleep forever
  //if we are sleeping we don't want to read the delay field of the instruction
  //if we stall, then don't delay and immediately re-exec
  delayCycles := Mux(sleeping, delayCycles - 1.U, Mux(io.stall, 0.U, io.instruction(11, 8)))

  //if we're sleeping, override instruction with `mov null null`
  //hacky? yes. efficient? no. functional? hopefully...
  val nopInstruction = "b101_00000_101_00_101".U
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
    io.isrSrc := instruction(7, 5)
    io.isrCtl.count := instruction(4, 0)
    io.isrCtl.doShift := true.B
  }.otherwise {
    io.isrSrc := 0.U
    io.isrCtl.count := 0.U
    io.isrCtl.doShift := false.B
  }
  //OUT
  when(opcode === 3.U) {
    io.osrDest := instruction(7, 5)
    io.osrCtl.count := instruction(4, 0)
    io.osrCtl.doShift := true.B
  }.otherwise {
    io.osrDest := 0.U
    io.osrCtl.count := 0.U
    io.osrCtl.doShift := false.B
  }

  //control signals for push/pull
  //lots of seemingly redundant signalling but we need to keep firrtl happy
  when(opcode === 4.U && instruction(7)) {
    io.osrCtl.iffeFlag := instruction(6)
    io.osrCtl.doPushPull := true.B
    io.isrCtl.iffeFlag := false.B
    io.isrCtl.doPushPull := false.B
  }.elsewhen(opcode === 4.U && !instruction(7)) {
      io.isrCtl.iffeFlag := instruction(6)
      io.isrCtl.doPushPull := true.B
      io.osrCtl.iffeFlag := false.B
      io.osrCtl.doPushPull := false.B
    }
    .otherwise {
      io.osrCtl.iffeFlag := false.B
      io.osrCtl.doPushPull := false.B
      io.isrCtl.iffeFlag := false.B
      io.isrCtl.doPushPull := false.B
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
