package dev.joeyh.pio.execution

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ProgramCounterTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "program counter"

  it should "count" in {
    test(new ProgramCounter) { uut =>
      //set wrap target to 0 (disable) and write enable low
      uut.io.wrapCfg.enable.poke(0)
      uut.io.write.enable.poke(false)
      uut.io.increment.poke(true)

      uut.clock.step()
      uut.io.read.expect(1, "counter should have incremented")
      uut.clock.step(9)
      uut.io.read.expect(10, "counter should have incremented")

      uut.io.increment.poke(false)
      uut.clock.step(10)
      uut.io.read.expect(10, "counter should not have incremented")

      uut.io.increment.poke(true)
      uut.clock.step(21)
      uut.io.read.expect(31, "counter should have incremented")

      uut.clock.step(1)
      uut.io.read.expect(0, "counter should wrap to 0")
    }
  }

  it should "wrap" in {
    test(new ProgramCounter) { uut =>
      //set wrap target to 10, disable write, allow auto increment
      uut.io.wrapCfg.enable.poke(1)
      uut.io.wrapCfg.trigger.poke(10)
      uut.io.wrapCfg.target.poke(0)

      uut.io.write.enable.poke(false)
      uut.io.increment.poke(true)

      uut.clock.step()
      uut.io.read.expect(1, "counter should have incremented")
      uut.clock.step(9)
      uut.io.read.expect(10, "counter should equal wrap target")
      uut.clock.step(1)
      uut.io.read.expect(0, "counter should wrap back to 0")

    }
  }

  it should "jump" in {
    test(new ProgramCounter) { uut =>
      //disable wrap and write, allow auto increment
      uut.io.wrapCfg.enable.poke(0)
      uut.io.write.enable.poke(false)
      uut.io.increment.poke(true)

      uut.clock.step(10)
      uut.io.read.expect(10, "counter should have incremented")

      uut.io.write.enable.poke(true)
      uut.io.write.data.poke(20)
      uut.clock.step(1)
      uut.io.read.expect(20, "counter should jump to 20")

      uut.clock.step(2)
      uut.io.read.expect(20, "counter still be 20 as we are still writing")

      uut.io.write.enable.poke(false)
      uut.clock.step(2)
      uut.io.read.expect(22, "counter should increment when not writing ")
    }
  }

  it should "ignore wrap when jumping" in {
    test(new ProgramCounter) { uut =>
      //enable wrap and increment, disable write
      uut.io.wrapCfg.enable.poke(1)
      uut.io.wrapCfg.trigger.poke(12)
      uut.io.wrapCfg.target.poke(2)

      uut.io.write.enable.poke(false)
      uut.io.increment.poke(true)

      uut.clock.step(10)
      uut.io.read.expect(10, "counter should have incremented")

      uut.io.write.enable.poke(true)
      uut.io.write.data.poke(20)
      uut.clock.step(1)
      uut.io.read.expect(20, "counter should jump to 20")
      uut.io.write.enable.poke(false)

      uut.clock.step(2)
      uut.io.read.expect(22, "counter should increment when not writing ")

      uut.clock.step(20)
      uut.io.read.expect(10, "counter should wrap around 31")

      uut.clock.step(5)
      uut.io.read.expect(4, "counter should wrap around 12")

    }
  }

}
