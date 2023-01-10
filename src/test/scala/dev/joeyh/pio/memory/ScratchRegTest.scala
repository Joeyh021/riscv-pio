package dev.joeyh.pio.memory

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ScratchRegTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "scratch register"

  it should "read and write" in {
    test(new ScratchReg) { uut =>
      //write 42 to reg
      uut.io.write.data.poke(42)
      uut.io.write.enable.poke(true)

      uut.io.read.expect(0, "Clock not yet stepped, output should be 0")

      uut.clock.step()
      uut.io.read.expect(42, "Clock stepped, value 42 should be written")

      uut.io.write.data.poke(67)
      //write 67
      uut.clock.step()
      uut.io.read.expect(67, "Clock stepped with write enable still high, value 67 should be written")

      //78 should not be written
      uut.io.write.enable.poke(false)
      uut.io.write.data.poke(78)

      uut.clock.step()

      uut.io.read.expect(67, "Clock stepped with write enable low, value 67 should remain")

      uut.clock.step(10)
      uut.io.read.expect(67, "Clock stepped with write enable low, value 67 should remain")

    }
  }

  it should "reset to 0" in {
    test(new ScratchReg) { uut =>
      //write 42 to reg
      uut.io.write.data.poke(42)
      uut.io.write.enable.poke(true)

      uut.io.read.expect(0, "Clock not yet stepped, output should be 0")

      uut.clock.step()
      uut.io.read.expect(42, "Clock stepped, value 42 should be written")

      //testing synchronous reset - we don't expect an async reset here
      uut.reset.poke(true.B)
      uut.io.read.expect(42, "Clock not stepped, value 42 should remain")

      uut.clock.step()
      uut.io.read.expect(0, "Device should have reset")
      uut.io.write.data.poke(67)
      uut.clock.step(10)
      uut.io.read.expect(0, "Device should have reset")

      uut.reset.poke(false.B)
      uut.clock.step(10)
      uut.io.read.expect(67, "Reset sent low, 67 should be written ")

    }
  }

}
