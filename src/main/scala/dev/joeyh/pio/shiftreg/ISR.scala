package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._

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

  val saturatingShiftCountSum = 32.U.min(io.sCtl.count + shiftCountReg)

  //wrap that threshold
  //when shift, shift the register
  when(io.sCtl.doShift) {
    //mask off N LSBs of input to shift in
    val inData = (io.shiftInData & ((1.U << io.sCtl.count) - 1.U))
    //when true, shift right
    //when false, shift left
    val shiftedReg = Mux(io.cfg.dir, reg >> io.sCtl.count, reg << io.sCtl.count)

    //update registers
    shiftCountReg := saturatingShiftCountSum
    //need to move up to put in MSBs if from the right, else can leave in LSBs
    reg := shiftedReg | inData << (32.U - Mux(io.cfg.dir, io.sCtl.count, 32.U))
  }

  //if the thresh input is 0, then it's *actually 32*
  val thresholdReached = Mux(
    io.cfg.thresh === 0.U,
    saturatingShiftCountSum === 32.U, //cannot be greater than 32
    saturatingShiftCountSum >= io.cfg.thresh
  )

  //push if either:
  //we were told to push directly, checking the ifFull condition if flag is set
  //or, autopush is enabled and we have reached the threshold
  val doPush = (io.pCtl.doPushPull && (Mux(io.pCtl.iffeFlag, thresholdReached, true.B))) || (io.cfg.autoEnabled && thresholdReached)

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
