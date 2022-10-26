package axi

import chisel3._
import chisel3.util._
import chisel3.util.experimental.forceName

//simple peripheral with 4 64-bit registers
//could be parametrised over n registers
//axi-lite data width has to be either 32 or 64 bits
class AXILiteRegisterSlave extends Module {
  val addrWidth = 2
  val dataWidth = 64;
  val io        = IO(new AXILiteSlave(addrWidth, dataWidth))

  //register with a vector of 4 64 bit numbers
  val registers = RegInit(VecInit(Seq.fill(4)(0.U(addrWidth.W))))

  //generate ready signal for write address channel
  val writeAddrReady = RegInit(false.B)
  // registers can be written to when both write address and data are valid, a
  val canWrite = io.writeAddr.valid && io.writeData.valid && !writeAddrReady
  writeAddrReady := canWrite
  io.writeAddr.ready := writeAddrReady

  //register to store write address
  val writeAddr = RegInit(0.U)
  when(canWrite) { writeAddr := io.writeAddr.bits.addr }

  io.writeData.ready := RegNext(canWrite)

  //write to registers
  val doWrite        = io.writeData.ready && io.writeData.valid && writeAddrReady && io.writeAddr.valid
  val writeRegSelect = writeAddr

  //write strobing not implemented, write entire word
  when(doWrite) { registers(writeRegSelect) := io.writeData.bits.data }

  //generate write response signals
  io.writeResponse.bits := 0.U //never error
  val writeResponseValid = RegInit(false.B)
  writeResponseValid := doWrite && !writeResponseValid
  io.writeResponse.valid := writeResponseValid

  //generate read address ready signal
  val readAddrReady = RegInit(false.B)
  val canRead       = !readAddrReady && io.readAddr.valid
  readAddrReady := canRead
  io.readAddr.ready := readAddrReady

  //read address latch
  val readAddr = RegInit(0.U)
  when(canRead) { readAddr := io.readAddr.bits.addr }

  //generate read data valid and response signals
  val readValid = RegInit(false.B)
  val doRead    = readAddrReady && io.readAddr.valid && !readValid
  readValid := doRead
  io.readData.valid := readValid
  io.readData.bits.resp := 0.U

  val readRegSelect = io.readAddr.bits.addr
  val output        = RegNext(registers(readRegSelect))
  io.readData.bits.data := output
}
