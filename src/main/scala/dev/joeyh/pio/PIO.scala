package dev.joeyh.pio

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog
import dev.joeyh.pio.util.ReadWrite

//the top level PIO interface
class PIOIO extends Bundle {
  val pioClock = Input(Clock())
  //address CSR and instructions in one 8-bit address space
  val address = Input(UInt(8.W))
  val rw      = new ReadWrite(UInt(16.W))

  val rx = new fifo.ConsumerIO
  val tx = new fifo.ProducerIO

  val pins = Analog(32.W)
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
      1.U -> csr.io.read
    )
  )

  //this module exists within the system clock domain
  //the PIO constructed below runs within it's own clock domain
  val pioClockSlowed = ClockDivider(csr.io.clockDiv, io.pioClock)

  //does not matter which clock domain these are in because they have no implicit clock
  val txFifo = Module(new fifo.Fifo)
  txFifo.io.producerClock := this.clock
  txFifo.io.producerReset := this.reset
  txFifo.io.consumerClock := pioClockSlowed
  txFifo.io.consumerReset := reset
  txFifo.io.producer <> io.tx

  val rxFifo = Module(new fifo.Fifo)
  rxFifo.io.producerClock := pioClockSlowed
  rxFifo.io.producerReset := reset
  rxFifo.io.consumerClock := this.clock
  rxFifo.io.consumerReset := this.reset
  rxFifo.io.consumer <> io.rx

  // the pio clock domain
  //use the enable register as the reset
  withClockAndReset(pioClockSlowed, !csr.io.pioEnable) {
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

    //misc exec unit stuff
    execUnit.io.stall := isr.io.stall | osr.io.stall //either may cause a stall

    execUnit.io.osrEmpty := osr.io.shiftCountReg === 0.U
    execUnit.io.branchPin := csr.io.branchPin

    pins.io.sideSet := execUnit.io.sideSet //connect side set to pins

    //direct reads/writes go through the exec unit
    osr.io.rw <> execUnit.io.osr
    isr.io.rw <> execUnit.io.isr

    //osr can write to scratch registers
    scratchX.io.write.enable := execUnit.io.x.write.enable || (osr.io.shiftOut.enable && execUnit.io.osrDest === 1.U)
    scratchX.io.write.data := MuxCase(
      0.U,
      Seq(
        (osr.io.shiftOut.enable && execUnit.io.osrDest === 1.U) -> osr.io.shiftOut.data,
        execUnit.io.x.write.enable                              -> execUnit.io.x.write.data
      )
    )

    scratchY.io.write.enable := execUnit.io.y.write.enable || (osr.io.shiftOut.enable && execUnit.io.osrDest === 2.U)
    scratchY.io.write.data := MuxCase(
      0.U,
      Seq(
        (osr.io.shiftOut.enable && execUnit.io.osrDest === 2.U) -> osr.io.shiftOut.data,
        execUnit.io.y.write.enable                              -> execUnit.io.y.write.data
      )
    )

    //writes for pins, from osr and exec unit
    pins.io.write.enable := (osr.io.shiftOut.enable && execUnit.io.osrDest === 0.U) | execUnit.io.pins.write.enable
    pins.io.write.data := MuxCase(
      0.U,
      Seq(
        (osr.io.shiftOut.enable && execUnit.io.osrDest === 0.U) -> osr.io.shiftOut.data,
        execUnit.io.pins.write.enable                           -> execUnit.io.pins.write.data
      )
    )

    //reads for pins, scratch regs, exec unit and ISR
    execUnit.io.x.read := scratchX.io.read
    execUnit.io.y.read := scratchY.io.read
    execUnit.io.pins.read := pins.io.read
    isr.io.shiftIn := pins.io.read

    pins.io.cfg := csr.io.pinCfg

    pins.io.pins <> io.pins

  }

}
