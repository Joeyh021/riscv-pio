package dev.joeyh.pio.execution

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import _root_.dev.joeyh.pio.util._

class ExecTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "execution unit"

  //mostly the same tests from the decode
  //as this just wraps decode with PC and individual units

  it should "execute an unconditional jump" in {
    test(new ExecUnit) { uut =>
      //unconditionally jump to address 21
      val instruction = Integer.parseInt("0000000000010101", 2)
      uut.io.instruction.poke(instruction)

      //no stall or side set
      uut.io.stall.poke(false)
      uut.io.sideSet.expect(false)

      //all the signals that should be dead
      uut.io.isrCtl.count.expect(0, "shift in count should be 0")
      uut.io.osrCtl.count.expect(0, "shift out count should be 0")
      uut.io.pull.doPushPull.expect(false, "pull should be disabled")
      uut.io.push.doPushPull.expect(false, "push should be disabled")
      uut.io.x.write.enable.expect(false, "x should not be written")
      uut.io.y.write.enable.expect(false, "y should not be written")
      uut.io.osr.write.enable.expect(false, "osr should not be written")
      uut.io.isr.write.enable.expect(false, "isr should not be written")
      uut.io.pins.write.enable.expect(false, "pins should not be written")

      uut.clock.step()

      //pc should jump
      uut.io.instructionAddress.expect(21, "PC should be 21")
    }
  }

  it should "execute an conditional jump with a side set" in {
    test(new ExecUnit) { uut =>
      //jump to address 6 if X neq Y
      val instruction = Integer.parseInt("0001000010100110", 2)
      uut.io.instruction.poke(instruction)
      //set x =1, y =0
      uut.io.x.read.poke(1)
      uut.io.y.read.poke(0)

      //no stall but side set
      uut.io.stall.poke(false)
      uut.io.sideSet.expect(true, "side set pin should be high")

      //all the signals that should be dead
      uut.io.isrCtl.count.expect(0, "shift in count should be 0")
      uut.io.osrCtl.count.expect(0, "shift out count should be 0")
      uut.io.pull.doPushPull.expect(false, "pull should be disabled")
      uut.io.push.doPushPull.expect(false, "push should be disabled")
      uut.io.x.write.enable.expect(false, "x should not be written")
      uut.io.y.write.enable.expect(false, "y should not be written")
      uut.io.osr.write.enable.expect(false, "osr should not be written")
      uut.io.isr.write.enable.expect(false, "isr should not be written")
      uut.io.pins.write.enable.expect(false, "pins should not be written")

      uut.clock.step()
      uut.io.instructionAddress.expect(6, "PC should be 6")
    }
  }

  it should "execute a move from x to y with 1 cycle of delay" in {
    test(new ExecUnit) { uut =>
      //move from X to Y
      val instruction = Integer.parseInt("1010000100100000", 2)
      uut.io.instruction.poke(instruction)

      //set x = val
      val regval = RandomUInt(32)
      uut.io.x.read.poke(regval)

      //no stall or side set
      uut.io.stall.poke(false)
      uut.io.sideSet.expect(false)

      //all the signals that should be dead
      uut.io.isrCtl.count.expect(0, "shift in count should be 0")
      uut.io.osrCtl.count.expect(0, "shift out count should be 0")
      uut.io.pull.doPushPull.expect(false, "pull should be disabled")
      uut.io.push.doPushPull.expect(false, "push should be disabled")
      uut.io.x.write.enable.expect(false, "x should not be written")
      uut.io.osr.write.enable.expect(false, "osr should not be written")
      uut.io.isr.write.enable.expect(false, "isr should not be written")
      uut.io.pins.write.enable.expect(false, "pins should not be written")

      uut.io.y.write.data.expect(regval, "y write line should equal x val")
      uut.io.y.write.enable.expect(true, "y should be written")

      uut.clock.step()
      uut.io.instructionAddress.expect(1, "PC should be 1 ")
      uut.io.y.write.enable.expect(false, "y should not be written")
      //should be executing a NOP here
      uut.io.isrCtl.count.expect(0, "shift in count should be 0")
      uut.io.osrCtl.count.expect(0, "shift out count should be 0")
      uut.io.pull.doPushPull.expect(false, "pull should be disabled")
      uut.io.push.doPushPull.expect(false, "push should be disabled")
      uut.io.x.write.enable.expect(false, "x should not be written")
      uut.io.osr.write.enable.expect(false, "osr should not be written")
      uut.io.isr.write.enable.expect(false, "isr should not be written")
      uut.io.pins.write.enable.expect(false, "pins should not be written")

      uut.clock.step()
      uut.io.instructionAddress.expect(1, "PC should still be 1 because delay")
      uut.clock.step()

    }
  }

}
