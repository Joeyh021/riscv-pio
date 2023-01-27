package dev.joeyh.pio.shiftreg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import _root_.dev.joeyh.pio.util.RandomUInt

class OSRTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "input shift register"

  it should "pull from fifo" in {
    test(new OSR) { uut =>
      val regval = RandomUInt(32)
      uut.io.fifo.empty.poke(false.B)
      uut.io.fifo.read.poke(regval)

      uut.io.ctrl.doPushPull.poke(true.B)
      uut.io.ctrl.iffeFlag.poke(false.B)

      uut.io.fifo.doRead.expect(true.B, "should be reading from fifo")
      uut.clock.step()
      uut.io.rw.read.expect(regval, "should have written value")
    }
  }

  it should "stall when trying to pull from empty fifo" in {
    test(new OSR) { uut =>
      val regval = RandomUInt(32)
      uut.io.fifo.empty.poke(true.B)
      uut.io.fifo.read.poke(regval)

      uut.io.ctrl.doPushPull.poke(true.B)
      uut.io.ctrl.iffeFlag.poke(false.B)

      uut.io.fifo.doRead.expect(false.B, "should be reading from fifo")
      uut.io.stall.expect(true.B, "should be stalled")
      uut.clock.step()
      uut.io.rw.read.expect(0.U, "should not have written value")
    }
  }

}
