package dev.joeyh.pio

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tags.Slow

@Slow
class PIOTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "PIO Block "

  it should "emit a square wave" in {
    test(new PIO).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { uut =>
      uut.reset.poke(true.B)
      //program
      val program = Seq(
        "b111_0_0001_110_00001".U, //set pin to 1, delay 1 cycle
        "b111_0_0000_110_00000".U, //set pin to 0, delay 0 cycle
        "b000_0_0000_000_00000".U  //jump to address 0
      )
      //write program to pio
      uut.io.rw.write.enable.poke(true)

      program.zip(Seq(0.U, 1.U, 2.U)).foreach {
        case (a, i) =>
          uut.io.address.poke(i)
          uut.io.rw.write.data.poke(a)
          uut.clock.step()
      }
      //set csrs

      //clock divider to 2
      uut.io.address.poke(32)
      uut.io.rw.write.data.poke(2)
      uut.clock.step()

      //branch pin can be left
      //wrap target can be left
      //isr/osr config can be left
      //pin config
      //input can be left as zero (hopefully works)
      //output can base=0, count=1 (0000000100000000)
      uut.io.address.poke(36)
      uut.io.rw.write.data.poke("b00000001_00000000".U)
      uut.clock.step()
      uut.io.rw.write.enable.poke(false)

      //pull reset low and go
      uut.reset.poke(false.B)
      //should run?
      //check the waves hahaha
      uut.clock.step(20)
    }
  }

  it should "output from x and y with wrap" in {
    test(new PIO).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { uut =>
      uut.reset.poke(true.B)
      //program
      val program = Seq(
        "b111_0_0000_000_00001".U,  //write 1 to x register (set)
        "b111_0_0000_001_00010".U,  //write 2 to y register (set)
        "b101_0_0000_110_00_000".U, //write x register to pins with no delay (mov)
        "b101_0_0000_110_00_001".U  //write y register to pins with no delay (mov)
      )
      //write program to pio
      uut.io.rw.write.enable.poke(true)

      program.zipWithIndex.foreach {
        case (a, i) =>
          uut.io.address.poke(i)
          uut.io.rw.write.data.poke(a)
          uut.clock.step()
      }
      //set csrs

      //clock divider to 2
      uut.io.address.poke(32)
      uut.io.rw.write.data.poke(2)
      uut.clock.step()

      //branch pin can be left
      //wrap target needs to be set to 2, wrap trigger 3 (our loop)
      uut.io.address.poke(34)
      uut.io.rw.write.data.poke("b1_00000_00011_00010".U)
      uut.clock.step()
      //isr/osr config can be left

      //pin config
      //input can be left as zero
      //output as base=1, count=2
      uut.io.address.poke(36)
      uut.io.rw.write.data.poke("b00000010_00000001".U)
      uut.clock.step()
      uut.io.rw.write.enable.poke(false)

      //pull reset low and go
      uut.reset.poke(false.B)
      //should run?
      //check the waves hahaha
      uut.clock.step(20)
    }
  }

  it should "ws2812b" in {}

}
