package dev.joeyh.pio.execution

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WaitUnitTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "wait unit"

  it should "do nothing when disabled" in {
    test(new Wait) { uut =>
      uut.io.enable.poke(false)

      uut.io.pins.poke("b111111111111".U)
      uut.io.polarity.poke(1.U)
      uut.io.pinIdx.poke(0.U)
      uut.io.doStall.expect(false.B, "Nothing should happen, unit is disabled")

      uut.io.pinIdx.poke(2.U)
      uut.io.doStall.expect(false.B, "Nothing should happen, unit is disabled")

      uut.io.pinIdx.poke(2.U)
      uut.io.pins.poke("b00000000000000000000000".U)
      uut.io.polarity.poke(0.U)
      uut.io.doStall.expect(false.B, "Nothing should happen, unit is disabled")
    }
  }

  it should "not stall when condition met" in {
    test(new Wait) { uut =>
      uut.io.enable.poke(true)
      uut.io.pins.poke("b101010".U)

      uut.io.polarity.poke(1.U)
      uut.io.pinIdx.poke(3.U)
      uut.io.doStall.expect(false, "should not stall as condition met")

      uut.io.polarity.poke(1.U)
      uut.io.pinIdx.poke(5.U)
      uut.io.doStall.expect(false, "should not stall as condition met")

      uut.io.polarity.poke(0.U)
      uut.io.pinIdx.poke(2.U)
      uut.io.doStall.expect(false, "should not stall as condition met")
    }
  }

  it should "stall when enabled but condition not met" in {
    test(new Wait) { uut =>
      uut.io.enable.poke(true)
      uut.io.pins.poke("b101010101010".U)

      uut.io.polarity.poke(1.U)
      uut.io.pinIdx.poke(2.U)
      uut.io.doStall.expect(true, "should stall as condition not met")

      uut.io.polarity.poke(0.U)
      uut.io.pinIdx.poke(11.U)
      uut.io.doStall.expect(true, "should stall as condition not met")

    }
  }

}
