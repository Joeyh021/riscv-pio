package gcd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import blinky.Blink

class BlinkTest extends AnyFlatSpec with ChiselScalatestTester {

  it should "blink when enabled" in {
    test(new Blink(1, 1)) { uut =>
      uut.io.enable.poke(true.B)
      uut.io.led0.expect(true.B)
      uut.clock.step()
      uut.io.led0.expect(false.B)
      uut.clock.step()
      uut.io.led0.expect(true.B)
    }
  }

  it should "not blink when disabled" in {
    test(new Blink(1, 1)) { uut =>
      uut.io.enable.poke(false.B)
      uut.io.led0.expect(true.B)
      uut.clock.step()
      uut.io.led0.expect(true.B)
      uut.clock.step()
      uut.io.led0.expect(true.B)
    }
  }

  it should "blink faster with faster clock" in {
    test(new Blink(10, 1)) { uut =>
      uut.io.enable.poke(true.B)
      uut.io.led0.expect(true.B)
      uut.clock.step(9)
      uut.io.led0.expect(true.B)
      uut.clock.step(1)
      uut.io.led0.expect(false.B)
      uut.clock.step(15)
      uut.io.led0.expect(true.B)
    }
  }

  it should "blink slower with a higher time" in {
    test(new Blink(1, 2)) { uut =>
      uut.io.enable.poke(true.B)
      uut.io.led0.expect(true.B)
      uut.clock.step(2)
      uut.io.led0.expect(true.B)
      uut.clock.step(1)
      uut.io.led0.expect(false.B)
      uut.clock.step(15)
      uut.io.led0.expect(true.B)
    }
  }

}
