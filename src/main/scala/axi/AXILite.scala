/// adapted from https://github.com/maltanar/axi-in-chisel
/// Used under MIT License - https://github.com/maltanar/axi-in-chisel/blob/master/LICENSE
/// https://zipcpu.com/blog/2020/03/08/easyaxil.html used as a reference

package axi

import chisel3._
import chisel3.util._
import chisel3.util.experimental.forceName

// address channel
class AXILiteAddress(addrWidth: Int) extends Bundle {
  val addr = UInt(addrWidth.W) //the actual address
  val prot = UInt(3.W)         //indicates protection level and type of transaction
}

// write channel
class AXILiteWriteData(dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
  val strb = UInt((dataWidth / 8).W) //specify which bytes of data contain info
}

//read channel
class AXILiteReadData(dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
  val resp = UInt(2.W) //indicates status of write response
}

//AXI lite I/O bundle
// Uses decoupled for read/valid along with above definitions
//decoupled is producer/output by default
//clock and reset (active low) are implicit
class AXILiteSlave(addrWidth: Int, dataWidth: Int) extends Bundle {
  val readAddr      = Flipped(Decoupled(new AXILiteAddress(addrWidth)))   //in
  val readData      = Decoupled(new AXILiteReadData(dataWidth))           //out
  val writeAddr     = Flipped(Decoupled(new AXILiteAddress(addrWidth)))   //in
  val writeData     = Flipped(Decoupled(new AXILiteWriteData(addrWidth))) //in
  val writeResponse = Decoupled(UInt(2.W))                                //out, write response channel

  forceName(writeAddr.bits.addr, "S_AXI_AWADDR")
  // rename signals to be compatible with those in the Xilinx template

}

//simple peripheral with 4 64-bit registers
//could be parametrised over n registers
//axi-lite data width has to be either 32 or 64 bits
object AXILiteSlaveRegisters extends Module {
  val addrWidth = 2
  val dataWidth = 64;
  val io        = new AXILiteSlave(addrWidth, dataWidth);

  val registers = Vec(4, RegInit(0.U(64.W)))

  //todo actual axi logic

}
