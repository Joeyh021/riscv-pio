package dev.joeyh.axi

import chisel3._
import chisel3.util._

//simple peripheral with 4 64-bit registers
//could be parametrised over n registers
//axi-lite data width has to be either 32 or 64 bits
//byte strobing not implemented
class AXILiteRegisterSlave extends RawModule {
  val addrWidth = 2
  val dataWidth = 32;
  val io        = IO(new AXILiteSlave(addrWidth, dataWidth))

  withClockAndReset(io.clock, !io.reset) {
    //register with a vector of 4 64 bit numbers
    val registers = RegInit(VecInit(Seq.fill(4)(0.U(dataWidth.W))))

    //--- ready/valid handshake registers ---
    val writeAddrReadyReg = RegInit(false.B)
    val writeDataReadyReg = RegInit(false.B)
    val readAddrReadyReg  = RegInit(false.B)
    val readDataValidReg  = RegInit(false.B)
    val writeRespValidReg = RegInit(false.B)

    //--- write signalling ---

    //connect awready signal
    io.writeAddr.ready := writeAddrReadyReg

    // registers can be written to when both write address and data are valid
    // and whatever the hell this last condition means
    val canWrite = io.writeAddr.valid && io.writeData.valid && !writeAddrReadyReg
    writeAddrReadyReg := canWrite

    //when can write, then store the address that was written to us
    val writeAddrReg = RegInit(0.U)
    when(canWrite) { writeAddrReg := io.writeAddr.bits.addr }

    //handle wready, use same canWrite wire
    writeDataReadyReg := canWrite
    io.writeData.ready := writeDataReadyReg

    //write to registers when both write address and data channels are ready and valid
    val doWrite = writeDataReadyReg && io.writeData.valid && writeAddrReadyReg && io.writeAddr.valid
    when(doWrite) { registers(writeAddrReg) := io.writeData.bits.data }

    //handle write response
    io.writeResponse.bits := 0.U                       //never error
    writeRespValidReg := doWrite && !writeRespValidReg //same weird pattern -- eduardo plz help
    io.writeResponse.valid := writeRespValidReg

    // --- read signalling ---

    //handle rready
    val canRead = !readAddrReadyReg && io.readAddr.valid
    readAddrReadyReg := canRead
    io.readAddr.ready := readAddrReadyReg

    //latch the read address passed to us
    val readAddrReg = RegInit(0.U)
    when(canRead) { readAddrReg := io.readAddr.bits.addr }

    // read data is valid when address is valid and we are ready
    val doRead = readAddrReadyReg && io.readAddr.valid && !readDataValidReg
    readDataValidReg := doRead
    io.readData.valid := readDataValidReg

    io.readData.bits.resp := 0.U //always respond with okay

    ///register select
    val readRegSelect = io.readAddr.bits.addr
    io.readData.bits.data := RegNext(registers(readRegSelect)) //cannot have combinatorial paths from in->out
  }
}
