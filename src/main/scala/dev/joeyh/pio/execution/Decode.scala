package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._
import java.io.DataOutput

//instruction decode stage
//includes asserting control signals for certain instructions, or passing to ALU if needed
class DecodeIO extends Bundle {
  val instruction = Input(UInt(5.W))

  //high if PC is to be incremented
  //used to re-execute on a stall
  val increment = Output(Bool())

  //ALU outputs
  val ALUSrc  = Output(UInt(2.W))
  val ALUDest = Output(UInt(2.W))
  val ALUOp   = Output(UInt(3.W))

  //ALU computes wait conditions
  //this is asserted if we need to wait (de-assert increment)
  val doWait = Input(Bool())

  val sleeping = Output(Bool())

  //the side set output value
  //goes straight to the pin
  val sideSet = Output(Bool())

}

class Decode extends Module {
  val io = IO(new DecodeIO)

  //if an instruction includes an N cycle delay
  //then we execute NOPs (mov x, x) for the next N cycles
  val delayCycles = RegInit(0.U(4.W))
  val sleeping    = delayCycles =/= 0.U

  //decrement if sleeping as we do not wish to sleep forever
  //if we are sleeping we don't want to read the delay field of the instruction
  delayCycles := Mux(sleeping, delayCycles - 1.U, io.instruction(11, 8))

  //if we're sleeping, override instruction with `mov x,x`
  //hacky? yes. efficient? no. functional? hopefully...
  val nopInstruction = "b101_00000_111_00_111".U
  val instruction    = Mux(sleeping, nopInstruction, io.instruction)

  //opcode is top 3 bits
  val opcode = instruction(15, 13)

  //side set is bit 12
  io.sideSet := instruction(12)

  switch(opcode) {
    //JUMP
    is(0.U) {
      val condition = instruction(7, 5)
      val address   = instruction(4, 0)
    }

    //WAIT
    is(1.U) {
      val polarity = instruction(7)
      val source   = instruction(6, 5)
      val index    = instruction(4, 0)
    }

    //IN
    is(2.U) {
      val source   = instruction(7, 5)
      val bitCount = instruction(4, 0)
    }

    //OUT
    is(3.U) {
      val dest     = instruction(7, 5)
      val bitCount = instruction(4, 0)
    }

    //PUSH/PULL
    is(4.U) {
      //if full/if empty flag
      val iffe = instruction(6)
      //block flag
      val blk = instruction(5)

      //PULL
      when(instruction(7)) {}
      //PUSH
      .otherwise {}

    }

    //MOV
    is(5.U) {
      val dest = instruction(7, 5)
      val op   = instruction(4, 3)
      val src  = instruction(2, 0)
    }

    //SET
    is(7.U) {
      val dest = instruction(7, 5)
      val data = instruction(4, 0)
    }

  }
}
