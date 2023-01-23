package dev.joeyh.pio.fifo

import chisel3._
import chisel3.util._

//An asynchronous 4x32bit FIFO
//allows for read/writing in different clock domains
//https://github.com/chipsalliance/rocket-chip/blob/master/src/main/scala/util/AsyncQueue.scala

class AsyncCrossingIO extends Bundle {
  val producerClock = Input(Clock())
  val producerReset = Input(Bool())
  val producer      = new ProducerIO

  val consumerClock = Input(Clock())
  val consumerReset = Input(Bool())
  val consumer      = new ConsumerIO
}

class FIFO extends RawModule {
  val io = IO(new AsyncCrossingIO)
}
