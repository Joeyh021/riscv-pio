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

  //rename ports to match xilinx convention
  forceName(readAddr.bits.addr, "S_AXI_ARADDR")
  forceName(readAddr.bits.prot, "S_AXI_ARPROT")
  forceName(readAddr.ready, "S_AXI_ARREADY")
  forceName(readAddr.valid, "S_AXI_ARVALID")

  forceName(readData.bits.data, "S_AXI_RDATA")
  forceName(readData.bits.resp, "S_AXI_RRESP")
  forceName(readData.ready, "S_AXI_RREADY")
  forceName(readData.valid, "S_AXI_RVALID")

  forceName(writeAddr.bits.addr, "S_AXI_AWADDR")
  forceName(writeAddr.bits.prot, "S_AXI_AWPROT")
  forceName(writeAddr.ready, "S_AXI_AWREADY")
  forceName(writeAddr.valid, "S_AXI_AWVALID")

  forceName(writeData.bits.data, "S_AXI_WDATA")
  forceName(writeData.bits.strb, "S_AXI_WSTRB")
  forceName(writeData.ready, "S_AXI_WREADY")
  forceName(writeData.valid, "S_AXI_WVALID")

  forceName(writeResponse.bits, "S_AXI_BRESP")
  forceName(writeResponse.ready, "S_AXI_BREADY")
  forceName(writeResponse.valid, "S_AXI_BVALID")
}
