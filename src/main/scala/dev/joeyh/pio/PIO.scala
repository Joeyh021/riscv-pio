package dev.joeyh.pio

import chisel3._
import chisel3.util._
import dev.joeyh.pio.util.ReadWrite

//the top level PIO interface
class PIOIO extends Bundle {
  //address CSR and instructions in one 8-bit address space
  val address = Input(UInt(8.W))
  val rw      = new ReadWrite(UInt(16.W))

  val rx = new fifo.ConsumerIO
  val tx = new fifo.ProducerIO
}

//top level PIO module
//most of the subcomponents are connected in here
class PIO extends Module {
  val io = IO(new PIOIO)

  //these are written by system and read combinationally by PIO
  //so they should be in system clock domain
  val csr          = Module(new memory.CSR)
  val instructions = Module(new memory.InstructionMem)

  //split this address space in half
  //we have 8 bits, so 256 possible 16-bit words
  //split into 32-bit spaces using top 3 bits as tag
  //the lower 32 are instruction memory
  //next 32 are CSR

  instructions.io.writeAddress := io.address
  instructions.io.write.enable := io.rw.write.enable && io.address(7, 5) === 0.U
  instructions.io.write.data := io.rw.write.data

  csr.io.address := (io.address - 32.U)(2, 0)
  csr.io.write.enable := io.rw.write.enable && io.address(7, 5) === 1.U
  csr.io.write.data := io.rw.write.data

  //mux for splitting reads
  io.rw.read := MuxLookup(
    io.address(7, 5),
    0.U,
    Seq(
      0.U -> instructions.io.read,
      1.U -> instructions.io.read
    )
  )

  //this module exists within the system clock domain
  //the PIO constructed below runs within it's own clock domain
  val (pioClock, pioReset) = ClockDivider(csr.io.clockDiv, clock, this.reset.asBool)

  //does not matter which clock domain these are in because they have no implicit clock
  val txFifo = Module(new fifo.Fifo)
  txFifo.io.producerClock := this.clock
  txFifo.io.producerReset := this.reset
  txFifo.io.consumerClock := pioClock
  txFifo.io.consumerReset := pioReset
  txFifo.io.producer <> io.tx

  val rxFifo = Module(new fifo.Fifo)
  rxFifo.io.producerClock := pioClock
  rxFifo.io.producerReset := pioReset
  rxFifo.io.consumerClock := this.clock
  rxFifo.io.consumerReset := this.reset
  rxFifo.io.consumer <> io.rx

  // the pio clock domain
  withClockAndReset(pioClock, pioReset) {
    val isr      = Module(new shiftreg.ISR)
    val osr      = Module(new shiftreg.OSR)
    val scratchX = Module(new memory.ScratchReg)
    val scratchY = Module(new memory.ScratchReg)
    val pins     = Module(new Pins)
    val execUnit = Module(new execution.ExecUnit)

    //instruction reads
    execUnit.io.instruction := instructions.io.read
    instructions.io.readAddress := execUnit.io.instructionAddress
    execUnit.io.wrapCfg := csr.io.wrapCfg

    //shift/fifo control
    isr.io.ctrl := execUnit.io.isrCtl
    isr.io.fifo <> rxFifo.io.producer
    isr.io.cfg := csr.io.isrCfg

    osr.io.ctrl := execUnit.io.osrCtl
    osr.io.fifo <> txFifo.io.consumer
    osr.io.cfg := csr.io.osrCfg

    //other exec unit inputs
    execUnit.io.osrEmpty := osr.io.shiftCountReg === 0.U
    execUnit.io.branchPin := csr.io.branchPin

    //stall from shift regs
    execUnit.io.stall := isr.io.stall || osr.io.stall

    //most reads/writes go through the exec unit
    osr.io.rw <> execUnit.io.osr
    isr.io.rw <> execUnit.io.isr
    scratchX.io <> execUnit.io.x
    scratchY.io <> execUnit.io.y

    //misc exec unit stuff
    execUnit.io.stall := isr.io.stall | osr.io.stall //either may cause a stall

    execUnit.io.osrEmpty := osr.io.shiftCountReg === 0.U
    execUnit.io.branchPin := csr.io.branchPin

    pins.io.sideSet := execUnit.io.sideSet //connect side set to pins

    //writes for pins, from osr and exec unit
    pins.io.write.enable := execUnit.io.osrCtl.doShift | execUnit.io.pins.write.enable
    pins.io.write.data := MuxCase(
      0.U,
      Seq(
        execUnit.io.osrCtl.doShift    -> osr.io.shiftOutData,
        execUnit.io.pins.write.enable -> execUnit.io.pins.write.data
      )
    )

    //reads for pins, exec unit and ISR
    execUnit.io.pins.read := pins.io.read
    isr.io.shiftInData := pins.io.read

    pins.io.cfg := csr.io.pinCfg

  }

}
