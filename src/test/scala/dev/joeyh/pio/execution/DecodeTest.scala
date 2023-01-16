package dev.joeyh.pio.execution

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class DecodeTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "instruction decoder"

  it should "decode an unconditional jump" in {
    test(new Decode) { uut =>
      //unconditionally jump to address 21
      val instruction = Integer.parseInt("0000000000010101", 2)
      uut.io.instruction.poke(instruction)
      //no stall or side set
      uut.io.stall.poke(false)
      uut.io.sideSet.expect(false)

      //pc should increment
      uut.io.increment.expect(true, "PC should be incremented")

      uut.io.branchOp.expect(0, "branchOp should be 0 (unconditional)")
      uut.io.branchAddress.expect(21, "branch address should be 21")
      uut.io.branchEnable.expect(true, "branch unit should be enabled")

      //all other signals should be low
      uut.io.waitEnable.expect(false, "wait unit should be disabled")
      uut.io.movEnable.expect(false, "mov unit should be disabled")
      uut.io.inCount.expect(0, "shift in count should be 0")
      uut.io.outCount.expect(0, "shift out count should be 0")
      uut.io.doPull.expect(false, "pull should be disabled")
      uut.io.doPush.expect(false, "push should be disabled")
    }
  }

  it should "decode an conditional jump with a side set" in {
    test(new Decode) { uut =>
      //jump to address 6 if X neq Y
      val instruction = Integer.parseInt("0001000010100110", 2)

      uut.io.instruction.poke(instruction)
      //no stall but side set
      uut.io.stall.poke(false)
      uut.io.sideSet.expect(true, "side set pin should be high")

      //pc should increment
      uut.io.increment.expect(true, "PC should be incremented")

      uut.io.branchOp.expect(5, "branchOp should be 5 (X != Y)")
      uut.io.branchAddress.expect(6, "branch address should be 6")
      uut.io.branchEnable.expect(true, "branch unit should be enabled")

      //all other signals should be low
      uut.io.waitEnable.expect(false, "wait unit should be disabled")
      uut.io.movEnable.expect(false, "mov unit should be disabled")
      uut.io.inCount.expect(0, "shift in count should be 0")
      uut.io.outCount.expect(0, "shift out count should be 0")
      uut.io.doPull.expect(false, "pull should be disabled")
      uut.io.doPush.expect(false, "push should be disabled")
    }
  }

  it should "decode a wait" in {
    test(new Decode) { uut =>
      //wait for pin 0 to be pulled high
      val instruction = Integer.parseInt("0010000010000000", 2)

      uut.io.instruction.poke(instruction)
      //no stall or side set
      uut.io.stall.poke(false)
      uut.io.sideSet.expect(false)

      uut.io.waitEnable.expect(true, "wait unit should be enabled")
      uut.io.waitPolarity.expect(true, "wait polarity should be high")
      uut.io.waitIdx.expect(0, "wait index should be 0")

      //all other signals should be low
      uut.io.branchEnable.expect(false, "branch unit should be disabled")
      uut.io.movEnable.expect(false, "mov unit should be disabled")
      uut.io.inCount.expect(0, "shift in count should be 0")
      uut.io.outCount.expect(0, "shift out count should be 0")
      uut.io.doPull.expect(false, "pull should be disabled")
      uut.io.doPush.expect(false, "push should be disabled")
    }
  }

  it should "decode a wait with delay" in {
    test(new Decode) { uut =>
      //wait for pin 1 to be pulled low
      //include 3 delay cycles
      val instruction = Integer.parseInt("0010001100000001", 2)
      uut.io.instruction.poke(instruction)

      uut.io.sideSet.expect(false)

      uut.io.waitEnable.expect(true, "wait unit should be enabled")
      uut.io.waitPolarity.expect(false, "wait polarity should be low")
      uut.io.waitIdx.expect(1, "wait index should be 1")

      //assume condition not met and we stall
      uut.io.stall.poke(true)

      //pc should not increment and we should re-exec the same instruction
      uut.io.increment.expect(false, "PC should not be incremented as we stalled")

      //should still be emitting wait instruction
      uut.io.waitEnable.expect(true, "wait unit should be enabled")
      uut.io.waitPolarity.expect(false, "wait polarity should be low")
      uut.io.waitIdx.expect(1, "wait index should be 1")

      uut.clock.step()

      //drive stall low, wait condition met
      uut.io.stall.poke(false)

      //should still be emitting wait instruction
      uut.io.waitEnable.expect(true, "wait unit should be enabled")
      uut.io.waitPolarity.expect(false, "wait polarity should be low")
      uut.io.waitIdx.expect(1, "wait index should be 1")

      //pc will increment on the next clock cycle, was driven high by the prev step
      uut.io.increment.expect(true, "PC should not be incremented ready to execute after delay")

      uut.clock.step()

      //the PC will be presenting the next instruction
      //unconditionally jump to address 21
      val nextInstruction = Integer.parseInt("0000000000010101", 2)
      uut.io.instruction.poke(nextInstruction)

      //should now delay (nop (mov null null)) for three cycles
      uut.io.movSrc.expect(5, "mov src should be 101 (null)")
      uut.io.movDest.expect(5, "mov dest should be 101 (null)")
      uut.io.movEnable.expect(true, "mov unit should be enabled")

      uut.io.waitEnable.expect(false, "wait should be disabled")
      uut.io.branchEnable.expect(false, "branch should be disabled")

      uut.io.increment.expect(false, "PC should not be incremented as we are delaying")

      //same for 2 more cycles
      for (i <- 0 until 2) {
        uut.clock.step()
        uut.io.movSrc.expect(5, "mov src should be 101 (null)")
        uut.io.movDest.expect(5, "mov dest should be 101 (null)")
        uut.io.movEnable.expect(true, "mov unit should be enabled")
        uut.io.waitEnable.expect(false, "wait should be disabled")
        uut.io.branchEnable.expect(false, "branch should be disabled")
        uut.io.increment.expect(false, "PC should not be incremented as we are delaying")
      }

      uut.clock.step()
      //branch should now be executed

      //pc should increment
      uut.io.increment.expect(true, "PC should be incremented")

      uut.io.branchOp.expect(0, "branchOp should be 0 (unconditional)")
      uut.io.branchAddress.expect(21, "branch address should be 21")
      uut.io.branchEnable.expect(true, "branch unit should be enabled")

      uut.io.waitEnable.expect(false, "wait should be disabled")
      uut.io.movEnable.expect(false, "mov should be disabled")

    }
  }

  it should "decode an in" in {
    test(new Decode) { uut =>
      //shift in 31 bits from pin 1
      val instruction = Integer.parseInt("0100000000111111", 2)
      uut.io.instruction.poke(instruction)

      uut.io.stall.poke(false)

      //pc should increment
      uut.io.increment.expect(true, "PC should be incremented")
      uut.io.inCount.expect(31, "shift in count should be 31")
      uut.io.inSrc.expect(1, "shift in src should be 1")

      //all other signals should be low
      uut.io.branchEnable.expect(false, "branch unit should be disabled")
      uut.io.waitEnable.expect(false, "branch unit should be disabled")
      uut.io.movEnable.expect(false, "mov unit should be disabled")
      uut.io.outCount.expect(0, "shift out count should be 0")
      uut.io.doPull.expect(false, "pull should be disabled")
      uut.io.doPush.expect(false, "push should be disabled")
    }

  }

}
