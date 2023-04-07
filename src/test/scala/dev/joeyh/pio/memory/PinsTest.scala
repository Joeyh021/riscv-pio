package dev.joeyh.pio.memory

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tagobjects.Slow

import dev.joeyh.pio.memory.{PinIO, Pins}
class PinsModule extends Module {
  val io = IO(new PinIO)
  val p  = Module(new Pins)
  p.io <> io
}

class PinsTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "pins"

  it should "data should not overlap" taggedAs (Slow) in {
    test(new PinsModule).withAnnotations(Seq(VerilatorBackendAnnotation)) { uut =>
      uut.io.cfg.inBase.poke(0.U)
      uut.io.cfg.inCount.poke(1.U)

      uut.io.cfg.outBase.poke(2.U)
      uut.io.cfg.outCount.poke(1.U)

      uut.io.write.enable.poke(true.B)
      uut.io.write.data.poke(1.U)
      uut.io.read.expect(0.U)
    }
  }

}
