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

  //what follows is a very long and complex bit of control flow
  //it took me a while to get ALL the outputs to drive on each of these cases
  //tread carefully. It probably doesn't do what you think it does.
  //there are basically 5 cases, in order of priority
  //A - on a shift cycle AND autopull is enabled AND we're at threshold BEFORE the shift
  //B - just a write to the register
  //C - PULL from the FIFO, but only if NOT(autopull enabled AND shift count is 0)
  //D - do a shift, maybe autopull after it if that's enabled and we're at threshold after the shift
  //E - nothing, but we need to drive the data output anyway

  //if we're on a shift cycle AND autopull is enabled AND we're at threshold BEFORE the shift
  //then we try to refill and stall (don't do the shift). This overrides the normal shift behaviour
  when(io.ctrl.doShift && io.cfg.autoEnabled && thresholdReachedBeforeShift) {
    //CASE A
    when(io.fifo.empty) {
      io.fifo.doRead := false.B
    }.otherwise {
      io.fifo.doRead := true.B
      reg := io.fifo.read
      shiftCountReg := 0.U
    }
    //always stall (re-exec the shift with a full register)
    io.stall := true.B
    //have to always drive all outputs
    io.shiftOut.enable := false.B
    io.shiftOut.data := 0.U
  }
  //otherwise, handle the rest of the instructions as usual
  .otherwise {
    when(io.rw.write.enable) {
      //CASE B
      //handle MOV instruction (write direct)
      reg := io.rw.write.data
      shiftCountReg := 0.U
      io.shiftOut.enable := false.B
      io.shiftOut.data := 0.U
      io.stall := false.B
      io.fifo.doRead := false.B

    }.elsewhen(
        io.ctrl.doPushPull && Mux(io.ctrl.iffeFlag, thresholdReachedAfterShift, true.B) && !(io.cfg.autoEnabled && io.shiftCountReg === 0.U)
      ) {
        //CASE C
        //handle PULL instruction
        //if iff flag high, then we only pull if we're at threshold
        //also, when autopull is enabled, any PULL instruction is a no-op when the OSR is full
        when(io.fifo.empty) {
          io.stall := true.B //stall if empty on a PULL (always block)
          io.fifo.doRead := false.B
        }.otherwise {
          io.stall := false.B
          io.fifo.doRead := true.B
          reg := io.fifo.read
          shiftCountReg := 0.U
        }
        io.shiftOut.enable := false.B
        io.shiftOut.data := 0.U
      }
      .elsewhen(io.ctrl.doShift) {
        // CASE D
        //handle shift instruction
        when(io.cfg.dir) {
          //shift out right
          //mask off N LSBs and present at output
          io.shiftOut.data := reg & ((1.U << io.ctrl.count) - 1.U)
          io.shiftOut.enable := true.B
          reg := reg >> io.ctrl.count //move register right, fill with 0s
        }.otherwise {
          //shift out left
          //put N MSBs at output by shifting the bits down
          io.shiftOut.data := reg >> (32.U - io.ctrl.count)
          io.shiftOut.enable := true.B
          reg := reg << io.ctrl.count //move register left, fill with 0s
        }
        shiftCountReg := shiftCountReg + io.ctrl.count

        //handle an autopull after a shift is executed
        //can't autopull after a write or explicit pull
        when(io.cfg.autoEnabled && thresholdReachedAfterShift) {
          when(io.fifo.empty) {
            //don't stall if we can't autopull
            io.fifo.doRead := false.B
          }.otherwise {
            io.fifo.doRead := true.B
            reg := io.fifo.read
            shiftCountReg := 0.U
          }
        }.otherwise { io.fifo.doRead := false.B }

        io.stall := false.B

      }
      .otherwise({
        //CASE E
        //no instruction, just drive outputs
        io.shiftOut.enable := false.B
        io.shiftOut.data := 0.U
        io.stall := false.B
        io.fifo.doRead := false.B
      })

  }

}
