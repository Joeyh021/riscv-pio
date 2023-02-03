package dev.joeyh.pio

import chisel3._
import chisel3.util._

import dev.joeyh.pio.Pins
//the top level PIO interface
class PIOIO extends Bundle {}

//top level PIO module
//most of the subcomponents are connected in here
class PIO extends Module {
  val io = IO(new PIOIO)

  //this module exists within the system clock domain
  //the PIO constructed below runs within it's own clock domain
  val pioClock = ClockDivider(csr.io.clockDiv)
  val pioReset = this.reset //FIXME: not properly syncd with pio clock

  //these are written by system and read combinationally by PIO
  //so they should be in system clock domain
  val csr          = Module(new memory.CSR(10))
  val instructions = Module(new memory.InstructionMem)

  val txFifo = Module(new fifo.Fifo)
  txFifo.io.producerClock := this.clock
  txFifo.io.producerReset := this.reset
  txFifo.io.consumerClock := pioClock
  txFifo.io.consumerReset := pioReset

  val rxFifo = Module(new fifo.Fifo)
  rxFifo.io.producerClock := pioClock
  rxFifo.io.producerReset := pioReset
  rxFifo.io.consumerClock := this.clock
  rxFifo.io.consumerReset := this.reset

  withClockAndReset(pioClock, pioReset) {
    val isr      = Module(new shiftreg.ISR)
    val osr      = Module(new shiftreg.OSR)
    val scratchX = Module(new memory.ScratchReg)
    val scratchY = Module(new memory.ScratchReg)
    val pins     = Module(new Pins)
    val execUnit = Module(new execution.ExecUnit)

    //instruction reads
    execUnit.io.instruction := instructions.io.read
    instructions.io.address := execUnit.io.instructionAddress
    execUnit.io.wrapTarget := csr.io.wrapTarget

    //shift/fifo control
    isr.io.ctrl := execUnit.io.isrCtl
    isr.io.fifo <> rxFifo.io.producer
    isr.io.cfg := csr.io.isrCfg

    osr.io.ctrl := execUnit.io.osrCtl
    osr.io.fifo <> rxFifo.io.consumer
    osr.io.cfg := csr.io.osrCfg

    //other exec unit inputs
    execUnit.io.osrEmpty := osr.io.shiftCountReg === 0.U
    execUnit.io.branchPin := csr.io.branchPin

    //stall from shift regs
    execUnit.io.stall := isr.io.stall || osr.io.stall

    //direct reads/writes to/from shift registers
    osr.io.rw <> execUnit.io.osr
    isr.io.rw <> execUnit.io.isr

    //shift registers and exex unit needs read/write to pins and scratch registers

  }

}
