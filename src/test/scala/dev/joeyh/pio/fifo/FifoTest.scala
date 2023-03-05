package dev.joeyh.pio.fifo

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tagobjects.Slow
import _root_.dev.joeyh.LedFifoProducer

class FifoWrapper extends Module {
  val io = IO(new Bundle {
    val producer = new ProducerIO
    val consumer = new ConsumerIO
  })
  val fifo = Module(new Fifo)
  fifo.io.consumerClock := clock
  fifo.io.producerClock := clock
  fifo.io.producerReset := reset
  fifo.io.consumerReset := reset
  fifo.io.producer <> io.producer
  fifo.io.consumer <> io.consumer
}

class FifoTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Fifo"

  it should "read and write at either end" in {
    test(new FifoWrapper).withAnnotations(Seq(WriteVcdAnnotation)) { uut =>
      uut.io.consumer.empty.expect(true.B)
      uut.clock.step()
      uut.io.producer.write.poke("hdeadb0ef".U)
      uut.io.producer.doWrite.poke(true.B)
      uut.clock.step()
      uut.io.producer.write.poke("hdeadb1ef".U)
      uut.clock.step(3)
      uut.io.producer.write.poke(false.B)
      uut.io.consumer.doRead.poke(true.B)
      uut.io.consumer.read.expect("hdeadb0ef".U)
      uut.clock.step()
      uut.io.consumer.read.expect("hdeadb1ef".U)
      uut.clock.step()
      uut.io.consumer.doRead.poke(false.B)
      uut.io.consumer.empty.expect(false.B)

    }
  }

}
