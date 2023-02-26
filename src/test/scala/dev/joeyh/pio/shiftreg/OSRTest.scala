package dev.joeyh.pio.shiftreg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import _root_.dev.joeyh.pio.util.RandomUInt

class OSRTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "output shift register"

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

      uut.io.fifo.doRead.expect(false.B, "should not be reading from fifo")
      uut.io.stall.expect(true.B, "should be stalled")
      uut.clock.step()
      uut.io.rw.read.expect(0.U, "should not have written value")
    }
  }

  it should "shift to the right" in {
    test(new OSR) { uut =>
      val regval = "b10101010101010101".U

      uut.io.rw.write.enable.poke(true.B)
      uut.io.rw.write.data.poke(regval)
      uut.io.shiftCountReg.expect(32.U, "shift count should be 32 (empty)")

      uut.clock.step()
      uut.io.rw.write.enable.poke(false.B)

      uut.io.rw.read.expect(regval, "should have stored register value")
      uut.io.shiftCountReg.expect(0.U, "should have reset shift count")

      uut.io.ctrl.doShift.poke(true.B)
      uut.io.cfg.dir.poke(true.B)
      uut.io.ctrl.count.poke(3.U)

      uut.io.shiftOut.data.expect("b101".U, "should have shifted out 3 bits")
      uut.clock.step()
      uut.io.rw.read.expect("b10101010101010".U, "should have shifted register value right")

      uut.io.shiftOut.data.expect("b010".U, "should have shifted out 3 bits")
      uut.clock.step()
      uut.io.rw.read.expect("b10101010101".U, "should have shifted register value right")

    }
  }

  it should "shift to the left" in {
    test(new OSR) { uut =>
      val regval = "b10101010_00000000_00000000_00000000".U

      uut.io.rw.write.enable.poke(true.B)
      uut.io.rw.write.data.poke(regval)
      uut.io.shiftCountReg.expect(32.U, "shift count should be 32 (empty)")

      uut.clock.step()
      uut.io.rw.write.enable.poke(false.B)

      uut.io.rw.read.expect(regval, "should have stored register value")
      uut.io.shiftCountReg.expect(0.U, "should have reset shift count")

      uut.io.ctrl.doShift.poke(true.B)
      uut.io.cfg.dir.poke(false.B)
      uut.io.ctrl.count.poke(5.U)

      uut.io.shiftOut.data.expect("b10101".U, "should have shifted out 5 bits")
      uut.clock.step()
      uut.io.rw.read.expect("b01000000_00000000_00000000_00000000".U, "should have shifted register value right")

      uut.io.ctrl.count.poke(2.U)
      uut.io.shiftOut.data.expect("b01".U, "should have shifted out 2 bits")
      uut.clock.step()
      uut.io.rw.read.expect(0.U, "should have shifted register value right")

    }
  }

  it should "autopull after a shift" in {
    test(new OSR) { uut =>
      val regval = "b11011".U

      uut.io.rw.write.enable.poke(true.B)
      uut.io.rw.write.data.poke(regval)
      uut.io.shiftCountReg.expect(32.U, "shift count should be 32 (empty)")

      uut.clock.step()

      uut.io.cfg.autoEnabled.poke(true)
      uut.io.cfg.thresh.poke(4.U)
      uut.io.rw.write.enable.poke(false.B)
      uut.io.fifo.empty.poke(false.B)
      uut.io.fifo.read.poke("b11111".U)

      uut.io.rw.read.expect(regval, "should have stored register value")
      uut.io.shiftCountReg.expect(0.U, "should have reset shift count")

      uut.io.ctrl.doShift.poke(true.B)
      uut.io.cfg.dir.poke(true.B)
      uut.io.ctrl.count.poke(4.U)

      uut.io.shiftOut.data.expect("b1011".U, "should have shifted out 3 bits")
      uut.io.fifo.doRead.expect(true.B, "should be reading from fifo")
      uut.clock.step()
      uut.io.rw.read.expect("b11111".U, "should have read from fifo")

    }
  }
  it should "not autopull after a shift if threshold not reached" in {}
  it should "not autopull after a write" in {}
  it should "pull and stall if trying to shift when threshold is reached" in {}

}
