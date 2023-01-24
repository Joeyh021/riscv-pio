package dev.joeyh.pio.shiftreg

import chisel3._
import chisel3.util._
import _root_.dev.joeyh.pio.fifo.ConsumerIO
import _root_.dev.joeyh.pio.util.ReadWrite

//OSR PULLS from TX FIFO
class OSR extends Module {
  val io = IO(new OSRIO)

  val reg = RegInit(0.U(32.W))
  io.rw.read := reg

  //init to 32:  0 indicates full, 32 indicates empty
  val shiftCountReg = RegInit(32.U(6.W))
  io.shiftCountReg := shiftCountReg

  //includes the count that will be shifted in on next clock cycle, if there is a shift
  val saturatingShiftCountSum = Mux(io.ctrl.doShift, 32.U.min(shiftCountReg + io.ctrl.count), shiftCountReg)

  //if the thresh input is 0, then it's *actually 32*
  val thresholdReachedAfterShift = Mux(
    io.cfg.thresh === 0.U,
    saturatingShiftCountSum === 32.U, //cannot be greater than 32
    saturatingShiftCountSum >= io.cfg.thresh
  )
  //same as above but we don't need to do any addition to add the shift count
  val thresholdReachedBeforeShift = Mux(
    io.cfg.thresh === 0.U,
    shiftCountReg === 32.U,
    shiftCountReg >= io.cfg.thresh
  )

  //if we're on a shift cycle AND autopull is enabled AND we're at threshold BEFORE the shift
  //then we try to refill and stall (don't do the shift). This overrides the normal shift behaviour
  when(io.ctrl.doShift && io.cfg.autoEnabled && thresholdReachedBeforeShift) {
    when(io.fifo.empty) {
      io.fifo.doRead := false.B
    }.otherwise {
      io.fifo.doRead := true.B
      reg := io.fifo.read
      shiftCountReg := 0.U
    }
    //always stall (re-exec the shift with a full register)
    io.stall := true.B
  }.otherwise {
    //handle MOV instruction (write direct)
    when(io.rw.write.enable) {
      reg := io.rw.write.data
      shiftCountReg := 0.U
    }

    //handle PULL instruction
    //if iff flag high, then we only pull if we're at threshold
    //also, when autopull is enabled, any PULL instruction is a no-op when the OSR is full
    when( 
      io.ctrl.doPushPull && Mux(io.ctrl.iffeFlag, thresholdReachedAfterShift, true.B) && !(io.cfg.autoEnabled && io.shiftCountReg === 0.U)
    ) {
      when(io.fifo.empty) {
        io.stall := true.B //stall if empty on a PULL (always block)
        io.fifo.doRead := false.B
      }.otherwise {
        io.fifo.doRead := true.B
        reg := io.fifo.read
        shiftCountReg := 0.U
      }
    }

    //handle shift instruction
    when(io.ctrl.doShift) {
      when(io.cfg.dir) {
        //shift out right
        //mask off N LSBs and present at output
        io.shiftOutData := reg & ((1.U << io.ctrl.count) - 1.U)
        reg := reg >> io.ctrl.count //move register right, fill with 0s
      }.otherwise {
        //shift out left
        //put N MSBs at output by shifting the bits down
        io.shiftOutData := reg >> (32.U - io.ctrl.count)
        reg := reg << io.ctrl.count //move register left, fill with 0s
      }

      //handle an autopull after a shift
      //we will never autopull after a manual pull or mov, so this is okay to be inside this block
      when((io.cfg.autoEnabled && thresholdReachedAfterShift)) {
        when(io.fifo.empty) {
          //don't stall if we can't autopull
          io.fifo.doRead := false.B
        }.otherwise {
          io.fifo.doRead := true.B
          reg := io.fifo.read
          shiftCountReg := 0.U
        }
      }
    }

  }

}
