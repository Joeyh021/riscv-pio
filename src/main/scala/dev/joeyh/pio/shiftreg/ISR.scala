package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._
import _root_.dev.joeyh.pio.fifo.FIFOProducerIO
import _root_.dev.joeyh.pio.util.ReadWrite

// The 32bit shift register
class ISRIO extends ReadWrite(UInt(32.W)) {
  val shiftIn = Input(UInt(32.W))
  val fifo    = Flipped(new FIFOProducerIO)
  val ctrl    = new ShiftControl
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
  //always connected but dont push unless told to further down

  //when shift, shift the register
  when(io.ctrl.shift) {
    //mask off N LSBs of input to shift in
    val inData = io.shiftIn & ((1.U << io.ctrl.shiftCount) - 1.U)
    //when true, shift right
    //when false, shift left
    val shiftedReg = Mux(io.ctrl.shiftDir, reg >> io.ctrl.shiftCount, reg << io.ctrl.shiftCount)

    //update registers
    shiftCountReg := Mux(shiftCountReg === 32.U, 32.U, io.ctrl.shiftCount + shiftCountReg) //saturating
    reg := shiftedReg | inData
  }

  val thresholdReached = io.ctrl.autoThresh <= (shiftCountReg + io.ctrl.shiftCount)
  //push if either:
  //we were told to push directly, checking the ifFull condition if flag is set
  //or, autopush is enabled and we have reached the threshold
  val doPush = (io.ctrl.doPushPull && (Mux(io.ctrl.iffeFlag, thresholdReached, true.B))) || (io.ctrl.autoPullEnabled && thresholdReached)

  when(doPush) {
    //stall if full
    when(io.fifo.full) {
      io.ctrl.stall := true.B
    }.otherwise {
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
