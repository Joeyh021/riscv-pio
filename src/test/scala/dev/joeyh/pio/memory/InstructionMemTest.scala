package dev.joeyh.pio.memory

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import dev.joeyh.pio.util._

import dev.joeyh.pio.util.RandomUInt
class InstructionMemTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "instruction memory"

  it should "read and write" in {
    test(new InstructionMem) { uut =>
      val a1 = RandomUInt(5)
      val v1 = RandomUInt(16)

      //put value
      uut.io.address.poke(a1)
      uut.io.write.enable.poke(true)
      uut.io.write.data.poke(v1)
      uut.clock.step()

      //read value
      uut.io.read.expect(v1, "Unit should read value written")

      //disable write
      uut.io.write.enable.poke(false)
      uut.io.write.data.poke(0)
      uut.clock.step()

      //value should still be there
      uut.io.read.expect(v1, "Unit should read value written")

      val a2 = RandomUInt(5)
      val v2 = RandomUInt(16)

      uut.io.address.poke(a2)
      uut.io.write.data.poke(v2)

      uut.clock.step()

      uut.io.read.expect(0, "Unit should not read value written")

      uut.io.write.enable.poke(true)
      uut.clock.step()

      uut.io.read.expect(v2, "Unit should read value written")

    }
  }

}
