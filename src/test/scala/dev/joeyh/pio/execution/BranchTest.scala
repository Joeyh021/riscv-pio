package dev.joeyh.pio.execution

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BranchUnitTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "branching unit"

  val address  = scala.util.Random.between(0, math.pow(2, 5).toInt)
  val regValue = scala.util.Random.between(0, Int.MaxValue)

  it should "branch when register is zero" in {
    test(new Branch) { uut =>
      //enable, set branch address
      uut.io.enable.poke(true)
      uut.io.address.poke(address)

      //branch on x==0
      uut.io.op.poke(1.U)
      uut.io.x.read.poke(0.U)
      uut.io.PCWrite.data.expect(address, "Unit should write address as x == 0")
      uut.io.PCWrite.enable.expect(true.B, "PC write enable should be high")

      //branch on y==0
      uut.io.op.poke(3.U)
      uut.io.y.read.poke(0.U)
      uut.io.PCWrite.data.expect(address, "Unit should write address as y == 0")
      uut.io.PCWrite.enable.expect(true.B, "PC write enable should be high")

      //set condition to x==0, but don't trigger it
      uut.io.op.poke(1.U)
      uut.io.x.read.poke(regValue)
      uut.io.PCWrite.enable.expect(false.B, "PC write enable should be low")

      //as above but for y
      uut.io.op.poke(3.U)
      uut.io.y.read.poke(regValue)
      uut.io.PCWrite.enable.expect(false.B, "PC write enable should be low")
      uut.io.address.expect(address)
    }
  }

  it should "branch and increment when register is nonzero" in {
    test(new Branch) { uut =>
      //enable, set branch address
      uut.io.enable.poke(true)
      uut.io.address.poke(address)

      //branch on y!=0
      uut.io.op.poke(4.U)
      uut.io.y.read.poke(regValue)
      uut.io.PCWrite.data.expect(address, "Unit should write address as y != 0")
      uut.io.PCWrite.enable.expect(true.B, "PC write enable should be high")
      uut.io.y.write.enable.expect(true.B, "y write enable should be high as we are incrementing")
      uut.io.y.write.data.expect(regValue + 1, "y should be y++")

      //as above but for x
      uut.io.op.poke(2.U)
      uut.io.x.read.poke(regValue)
      uut.io.PCWrite.data.expect(address, "Unit should write address as x != 0")
      uut.io.PCWrite.enable.expect(true.B, "PC write enable should be high")
      uut.io.x.write.enable.expect(true.B, "x write enable should be high as we are incrementing")
      uut.io.x.write.data.expect(regValue + 1, "x should be x++")
    }
  }

  it should "not write unless we are incrementing" in {
    test(new Branch) { uut =>
      //enable, set branch address
      uut.io.enable.poke(true)
      uut.io.address.poke(address)

      //branch on y != 0
      uut.io.op.poke(4.U)
      uut.io.y.read.poke(0.U)

      uut.io.PCWrite.enable.expect(false.B, "PC write enable should be low")
      uut.io.y.write.enable.expect(false.B, "y write enable should be low as we are not incrementing")
      //write enables are low so actual write data is irrelevant

    }
  }

  it should "branch when registers are not equal" in {
    test(new Branch) { uut =>
      //enable, set branch address
      uut.io.enable.poke(true)
      uut.io.address.poke(address)

      //set registers to equal values
      uut.io.op.poke(5.U)
      uut.io.x.read.poke(regValue)
      uut.io.y.read.poke(regValue)

      uut.io.PCWrite.enable.expect(false.B, "PC write enable should be low")
      uut.io.x.write.enable.expect(false.B, "x write enable should be low as we are not incrementing")
      uut.io.y.write.enable.expect(false.B, "y write enable should be low as we are not incrementing")

      //set registers to unequal values
      uut.io.y.read.poke(regValue + 1)
      uut.io.PCWrite.enable.expect(true.B, "PC write enable should be high")
      uut.io.PCWrite.data.expect(address, "Unit should write address as x != y")

    }
  }

  it should "branch when branch pin is high" in {
    test(new Branch) { uut =>
      //enable, set branch address
      uut.io.enable.poke(true)
      uut.io.address.poke(address)

      uut.io.op.poke(6.U)

      //pins all high
      uut.io.pins.poke("b10".U)

      //branch on pin 0
      uut.io.branchPinCSR.poke(0.U)
      uut.io.PCWrite.enable.expect(false.B, "PC write enable should be low as pin 0 is low")

      //branch on pin 1
      uut.io.branchPinCSR.poke(1.U)
      uut.io.PCWrite.enable.expect(true.B, "PC write enable should be high as pin 1 is high")
    }
  }

}
