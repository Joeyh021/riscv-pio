package dev.joeyh.pio

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ClockDividerTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "clock divider"

  it should "divide by two" in {
    test(new ClockDivider) { uut =>
      uut.io.divisor.poke(3.U)
      uut.clock.step()
      uut.clock.step()
      uut.clock.step()
      uut.clock.step()
      uut.clock.step()
      uut.clock.step()
      uut.clock.step()

    }
  }

}
