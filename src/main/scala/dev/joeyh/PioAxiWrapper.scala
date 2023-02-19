package dev.joeyh

import axi.AXILiteSlave

import chisel3._
import chisel3.util._
import dev.joeyh.pio.PIO
import chisel3.util.experimental.forceName

class PioAxiWrapper extends Module {
  val io = IO(new Bundle {
    val axiLiteSlave    = new AXILiteSlave(8, 32)
    val axiStreamMaster = Output(Bool())
    val axiStreamSlave  = Input(Bool())
  })
  forceName(clock, "S_AXI_ACLK")
  forceName(reset, "S_AXI_ARESETN")

  //pio block
  val pio = Module(new PIO)

  //flip the reset for axi lite
  withReset(!reset.asBool) {

    //write signalling
    val addrWriteReady = RegInit(false.B)
    addrWriteReady := !addrWriteReady && (io.axiLiteSlave.writeAddr.valid && io.axiLiteSlave.writeData.valid) && (!io.axiLiteSlave.writeResponse.valid || io.axiLiteSlave.writeResponse.ready)
    io.axiLiteSlave.writeAddr.ready := addrWriteReady
    io.axiLiteSlave.writeData.ready := addrWriteReady

    //write response
    val writeResponseValid = RegInit(false.B)
    when(addrWriteReady) {
      writeResponseValid := true.B
    }.elsewhen(io.axiLiteSlave.writeResponse.ready) {
      writeResponseValid := false.B
    }
    io.axiLiteSlave.writeResponse.valid := writeResponseValid
    io.axiLiteSlave.writeResponse.bits := 0.U

    //read signalling
    val readAddrReady = !io.axiLiteSlave.readAddr.valid
    io.axiLiteSlave.readAddr.ready := readAddrReady
    val readReady = io.axiLiteSlave.readAddr.valid && io.axiLiteSlave.readAddr.ready

    val readValid = RegInit(false.B)
    when(readReady) {
      readValid := true.B
    }.elsewhen(io.axiLiteSlave.readData.ready) {
      readValid := false.B
    }
    io.axiLiteSlave.readData.valid := readValid

    val readData = RegInit(0.U(32.W))
    io.axiLiteSlave.readData.bits.data := readData
    io.axiLiteSlave.readData.bits.resp := 0.U

    //read/write register interface logic
    //ignore write strobing because i'm LAZY

    //pio address
    pio.io.address := MuxCase(
      0.U,
      Seq(
        readReady      -> io.axiLiteSlave.readAddr.bits.addr,
        addrWriteReady -> io.axiLiteSlave.writeAddr.bits.addr
      )
    )
    io.axiLiteSlave.readData.bits.data := pio.io.rw.read

    pio.io.rw.write.enable := addrWriteReady
    pio.io.rw.write.data := io.axiLiteSlave.writeData.bits.data
  }

  //
  pio.io.rx := DontCare
  pio.io.tx := DontCare
  io.axiStreamMaster := true.B
}
