package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._

//ISR PUSHES to RX FIFO
class ISR extends Module {
  val io = IO(new ISRIO)

  val reg           = RegInit(0.U(32.W))
  val shiftCountReg = RegInit(0.U(6.W))
  io.shiftCountReg := shiftCountReg

  //read/write direct to reg
  io.rw.read := reg
  when(io.rw.write.enable) {
    reg := io.rw.write.data
    shiftCountReg := 0.U
  }

  val saturatingShiftCountSum = 32.U.min(Mux(io.ctrl.doShift, io.ctrl.count, 0.U) + shiftCountReg)

  //when shift, shift the register
  when(io.ctrl.doShift) {
    //mask off N LSBs of input to shift in
    val inData = (io.shiftIn & ((1.U << io.ctrl.count) - 1.U))
    //when true, shift right
    //when false, shift left
    val shiftedReg = Mux(io.cfg.dir, reg >> io.ctrl.count, reg << io.ctrl.count)

    //update registers
    shiftCountReg := saturatingShiftCountSum
    //need to move up to put in MSBs if from the right, else can leave in LSBs
    reg := shiftedReg | inData << (32.U - Mux(io.cfg.dir, io.ctrl.count, 32.U))
  }

  //if the thresh input is 0, then it's *actually 32*
  val thresholdReached = Mux(
    io.cfg.thresh === 0.U,
    saturatingShiftCountSum === 32.U, //cannot be greater than 32
    saturatingShiftCountSum >= io.cfg.thresh
  )

  //push if either:
  //we were told to push directly, checking the ifFull condition if flag is set
  //or, autopush is enabled AND we have reached the threshold AND we were told to shift (don't push on a non-shift cycle)
  val doPush = (io.ctrl.doPushPull && Mux(io.ctrl.iffeFlag, thresholdReached, true.B)) || (io.cfg.autoEnabled && thresholdReached && io.ctrl.doShift)

  when(doPush) {
    //stall if full
    when(io.fifo.full) {
      io.stall := true.B
      io.fifo.doWrite := false.B
      io.fifo.write := 0.U
    }.otherwise {
      io.stall := false.B
      io.fifo.doWrite := true.B
      io.fifo.write := reg
      reg := 0.U
      shiftCountReg := 0.U
    }
  }.otherwise {
    io.fifo.doWrite := false.B
    io.stall := false.B
    io.fifo.write := 0.U
  }

}
