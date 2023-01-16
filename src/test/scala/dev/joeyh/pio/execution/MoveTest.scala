package dev.joeyh.pio.execution

package dev.joeyh.pio.execution

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MoveTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "move unit"

  val regValue       = scala.util.Random.between(0, Int.MaxValue)
  val pinsValue      = regValue & (math.pow(2, 12).toInt - 1)
  val immediateValue = scala.util.Random.between(0, (math.pow(2, 5).toInt - 1))

  it should "move from X to Y" in {
    test(new Move) { uut =>
      uut.io.src.poke(0)
      uut.io.dest.poke(1)
      uut.io.enable.poke(true)

      uut.io.x.read.poke(regValue)
      uut.io.y.write.enable.expect(true, "y write enable should be high")
      uut.io.y.write.data.expect(regValue, "y should be written to")

    }
  }

  it should "move from Y to X" in {
    test(new Move) { uut =>
      uut.io.src.poke(1)
      uut.io.dest.poke(0)
      uut.io.enable.poke(true)

      uut.io.y.read.poke(regValue)
      uut.io.x.write.enable.expect(true, "x write enable should be high")
      uut.io.x.write.data.expect(regValue, "x should be written to")
    }
  }

  it should "move from osr to pins" in {
    test(new Move) { uut =>
      uut.io.src.poke(3)  //osr
      uut.io.dest.poke(6) //pins
      uut.io.enable.poke(true)

      uut.io.osr.read.poke(regValue)
      uut.io.pins.write.enable.expect(true, "pins write enable should be high")
      uut.io.pins.write.data.expect(pinsValue, "pins should be written to")
    }
  }

  it should "move from pins to X" in {
    test(new Move) { uut =>
      uut.io.src.poke(6)  //pins
      uut.io.dest.poke(0) //x
      uut.io.enable.poke(true)

      uut.io.pins.read.poke(pinsValue)
      uut.io.x.write.enable.expect(true, "x write enable should be high")
      uut.io.x.write.data.expect(pinsValue, "x should be written to")
    }
  }

  it should "set Y" in {
    test(new Move) { uut =>
      uut.io.src.poke(4)  //immediate
      uut.io.dest.poke(1) //y
      uut.io.enable.poke(true)

      uut.io.immediate.poke(immediateValue)
      uut.io.y.write.enable.expect(true, "y write enable should be high")
      uut.io.y.write.data.expect(immediateValue, "y should be written to")
    }
  }

  it should "set pins" in {
    test(new Move) { uut =>
      uut.io.src.poke(4)  //immediate
      uut.io.dest.poke(6) //pins
      uut.io.enable.poke(true)

      uut.io.immediate.poke(immediateValue)
      uut.io.pins.write.enable.expect(true, "y write enable should be high")
      uut.io.pins.write.data.expect(immediateValue, "y should be written to")
    }
  }

  it should "move to null" in {
    test(new Move) { uut =>
      uut.io.src.poke(0)  //x
      uut.io.dest.poke(5) //null
      uut.io.enable.poke(true)

      uut.io.x.read.poke(regValue)

      //no write enables should be high
      uut.io.x.write.enable.expect(false, "x write enable should be low")
      uut.io.y.write.enable.expect(false, "y write enable should be low")
      uut.io.isr.write.enable.expect(false, "isr write enable should be low")
      uut.io.osr.write.enable.expect(false, "osr write enable should be low")
      uut.io.pins.write.enable.expect(false, "pins write enable should be low")
    }
  }

  it should "move from null to X" in {
    test(new Move) { uut =>
      uut.io.src.poke(5)  //null
      uut.io.dest.poke(0) //xx
      uut.io.enable.poke(true)

      //no write enables should be high
      uut.io.x.write.enable.expect(true, "x write enable should be high")
      uut.io.x.write.data.expect(0.U, "0 should be written to X")
    }
  }

  it should "do nothing when dest is immediate" in {
    test(new Move) { uut =>
      uut.io.src.poke(5)  //null
      uut.io.dest.poke(4) //immediate
      uut.io.enable.poke(true)

      //no write enables should be high
      uut.io.x.write.enable.expect(false, "x write enable should be low")
      uut.io.y.write.enable.expect(false, "y write enable should be low")
      uut.io.isr.write.enable.expect(false, "isr write enable should be low")
      uut.io.osr.write.enable.expect(false, "osr write enable should be low")
      uut.io.pins.write.enable.expect(false, "pins write enable should be low")
    }
  }
}
