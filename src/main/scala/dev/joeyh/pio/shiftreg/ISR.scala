package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._
import _root_.dev.joeyh.pio.fifo.FIFOProducerIO
import _root_.dev.joeyh.pio.util.ReadWrite

// The 32bit shift register
class ISRIO extends ReadWrite(UInt(32.W)) {
  val shiftInData = Input(UInt(32.W))
  val fifo        = Flipped(new FIFOProducerIO)
  val ctrl        = new ShiftControl
}

class ISR extends Module {
  val io = IO(new ISRIO)

  val reg           = RegInit(0.U(32.W))
  val shiftCountReg = RegInit(0.U(6.W))

  //read/write direct to reg
  io.read := reg
  when(io.write.enable) {
    reg := io.write.data
    shiftCountReg := 0.U
  }

  val saturatingShiftCountSum = 32.U.min(io.ctrl.shiftCount + shiftCountReg)

  //wrap that threshold
  //when shift, shift the register
  when(io.ctrl.shift) {
    //mask off N LSBs of input to shift in
    val inData = (io.shiftInData & ((1.U << io.ctrl.shiftCount) - 1.U))
    //when true, shift right
    //when false, shift left
    val shiftedReg = Mux(io.ctrl.shiftDir, reg >> io.ctrl.shiftCount, reg << io.ctrl.shiftCount)

    //update registers
    shiftCountReg := saturatingShiftCountSum
    //need to move up to put in MSBs if from the right, else can leave in LSBs
    reg := shiftedReg | inData << (32.U - Mux(io.ctrl.shiftDir, io.ctrl.shiftCount, 32.U))
  }

  //if the thresh input is 0, then it's *actually 32*
  val thresholdReached = Mux(
    io.ctrl.pushPullThresh === 0.U,
    saturatingShiftCountSum === 32.U, //cannot be greater than 32
    saturatingShiftCountSum >= io.ctrl.pushPullThresh
  )

  //push if either:
  //we were told to push directly, checking the ifFull condition if flag is set
  //or, autopush is enabled and we have reached the threshold
  val doPush = (io.ctrl.doPushPull && (Mux(io.ctrl.iffeFlag, thresholdReached, true.B))) || (io.ctrl.autoPushPullEnabled && thresholdReached)

  when(doPush) {
    //stall if full
    when(io.fifo.full) {
      io.ctrl.stall := true.B
      io.fifo.doWrite := false.B
      io.fifo.write := 0.U
    }.otherwise {
      io.ctrl.stall := false.B
      io.fifo.doWrite := true.B
      io.fifo.write := reg
      reg := 0.U
      shiftCountReg := 0.U
    }
  }.otherwise {
    io.fifo.doWrite := false.B
    io.ctrl.stall := false.B
    io.fifo.write := 0.U
  }

}
