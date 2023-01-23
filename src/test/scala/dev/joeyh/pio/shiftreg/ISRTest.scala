package dev.joeyh.pio.shiftreg

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import _root_.dev.joeyh.pio.util.RandomUInt

class ISRTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "input shift register"

  it should "push to fifo" in {
    test(new ISR) { uut =>
      val regval = RandomUInt(32)
      uut.io.write.enable.poke(true.B)
      uut.io.write.data.poke(regval)

      uut.clock.step()
      uut.io.read.expect(regval, "should have written value")

      uut.io.ctrl.doPushPull.poke(true.B)
      uut.io.ctrl.iffeFlag.poke(false.B)

      uut.io.fifo.full.poke(false.B)
      uut.io.fifo.doWrite.expect(true.B, "should be writing to fifo")
      uut.io.fifo.write.expect(regval, "should be writing to fifo")

      uut.clock.step()

      uut.io.read.expect(0.U, "ISR should have cleared on push")

    }
  }

  it should "not push when fifo empty" in {
    test(new ISR) { uut =>
      val regval = RandomUInt(32)
      uut.io.write.enable.poke(true.B)
      uut.io.write.data.poke(regval)

      uut.clock.step()
      uut.io.read.expect(regval)

      uut.io.ctrl.doPushPull.poke(true.B)
      uut.io.ctrl.iffeFlag.poke(false.B)

      uut.io.fifo.full.poke(true.B)
      uut.io.fifo.doWrite.expect(false.B, "fifo full, no write")
      uut.io.fifo.write.expect(0.U, "fifo full, no write")

      uut.clock.step()

      uut.io.read.expect(regval, "ISR should not have cleared")

    }
  }

  it should "not push when iff set and threshold not reached" in {
    test(new ISR) { uut =>
      val regval = RandomUInt(32)
      uut.io.write.enable.poke(true.B)
      uut.io.write.data.poke(regval)

      uut.clock.step()
      uut.io.read.expect(regval)

      uut.io.ctrl.pushPullThresh.poke(0.U)

      uut.io.ctrl.doPushPull.poke(true.B)
      uut.io.ctrl.iffeFlag.poke(true.B)

      uut.io.fifo.full.poke(false.B)
      uut.io.fifo.doWrite.expect(false.B, "iff high and threshold not reached, no write")

      uut.clock.step()

      uut.io.read.expect(regval, "ISR should not have cleared")

    }
  }

  it should "shift data left and right" in {
    test(new ISR) { uut =>
      uut.io.ctrl.shiftDir.poke(false.B)
      uut.io.ctrl.shiftCount.poke(2.U)
      uut.io.ctrl.shift.poke(true.B)
      uut.io.shiftInData.poke("b110110".U)

      uut.clock.step()

      uut.io.read.expect("b10".U, "should have put two lowest bits in LSB of register")

      uut.io.ctrl.shiftCount.poke(5.U)
      uut.io.shiftInData.poke("b001110".U)

      uut.clock.step()

      uut.io.read.expect("b01001110".U, "should have put 5 more bits in register")

      //switch up the direction
      uut.io.ctrl.shiftDir.poke(true.B)

      uut.io.ctrl.shiftCount.poke(2.U)
      uut.io.shiftInData.poke("b1101".U)

      uut.clock.step()

      uut.io.read.expect("b01000000_00000000_00000000_00010011".U, "should have put two lowest bits in MSB of register")

    }
  }

  it should "autopush" in {
    test(new ISR) { uut =>
      uut.io.ctrl.autoPushPullEnabled.poke(true.B)
      uut.io.ctrl.pushPullThresh.poke(16.U)
      uut.io.ctrl.doPushPull.poke(false.B)

      uut.io.shiftInData.poke("b111111111111111111111".U)
      uut.io.ctrl.shiftCount.poke(12.U)
      uut.io.ctrl.shiftDir.poke(false.B)
      uut.io.ctrl.shift.poke(true.B)

      uut.clock.step()
      uut.io.read.expect("b111111111111".U, "should have shifted in 12 bits")
      uut.io.ctrl.shiftCount.poke(3.U)

      uut.io.fifo.doWrite.expect(false.B, "should not yet be writing to fifo")
      uut.io.ctrl.shiftCount.poke(4.U)
      uut.io.shiftInData.poke(0.U)
      uut.io.fifo.doWrite.expect(true.B, "should be writing to fifo, threshold of 16 reached")
      uut.io.fifo.write.expect("b0000111111111111".U, "should be writing pushed in data to fifo")
      uut.clock.step()
      uut.io.ctrl.shift.poke(false.B)
      uut.io.read.expect(0.U, "should have cleared ISR")
    }
  }

}
