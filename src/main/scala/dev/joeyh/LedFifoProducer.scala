package dev.joeyh

import chisel3._
import chisel3.util._
import dev.joeyh.pio.fifo.ProducerIO

class LedFifoProducer(nLeds: Int) extends Module {
  val io = IO(Flipped(new ProducerIO))

  val colours = VecInit(
    Seq(
      "h00ff00".U, //red
      "hff0000".U, //green
      "h0000ff".U, //blue
      "hffffff".U  //white
    )
  )

  val writing                          = RegInit(true.B)
  val (sleepCounter, sleepCounterWrap) = Counter(0 to 100000, enable = !writing)

  when(sleepCounterWrap) {
    writing := true.B
  }

  val (writeCounter, writeCounterWrap) = Counter(0 until nLeds, enable = writing)
  when(writeCounterWrap) {
    writing := false.B
  }

  val idx = RegInit(0.U(2.W))
  io.doWrite := !io.full && writing
  when(writing) {
    io.write := colours(idx)
    idx := idx + io.doWrite
  }.otherwise {
    io.write := 0.U
    idx := 0.U
  }
}
