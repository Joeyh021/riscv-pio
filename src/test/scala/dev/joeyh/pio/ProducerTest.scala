package dev.joeyh.pio

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tagobjects.Slow
import _root_.dev.joeyh.LedFifoProducer

class ProducerTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "LED Producer"

  it should "write data out" in {
    test(new LedFifoProducer(5)).withAnnotations(Seq(WriteVcdAnnotation)) { uut =>
      uut.clock.setTimeout(0)
      uut.io.full.poke(false.B)

      uut.io.doWrite.expect(true.B)
      uut.io.write.expect("h00ff00".U)
      uut.clock.step()

      uut.io.doWrite.expect(true.B)
      uut.io.write.expect("hff0000".U)
      uut.clock.step()

      uut.io.doWrite.expect(true.B)
      uut.io.write.expect("h0000ff".U)
      uut.clock.step()

      uut.io.doWrite.expect(true.B)
      uut.io.write.expect("hffffff".U)
      uut.clock.step()

      uut.io.full.poke(true.B)
      uut.clock.step()
      uut.io.doWrite.expect(false.B)
      uut.clock.step()
      uut.io.doWrite.expect(false.B)

      uut.io.full.poke(false.B)
      uut.io.doWrite.expect(false.B)
      uut.clock.step(100000 * 2)

    }
  }

}
