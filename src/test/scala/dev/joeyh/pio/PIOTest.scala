package dev.joeyh.pio

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PIOTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "PIO Block "

  it should "emit a square wave" in {
    test(new PIO).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { uut =>
      //program
      val program = Seq(
        "b111_0_0001_110_00001".U, //set pin to 1, delay 1 cycle
        "b111_0_0000_110_00000".U, //set pin to 0, delay 0 cycle
        "b000_0_0000_000_00000".U  //jump to address 0
      )
      //write program to pio
      uut.io.rw.write.enable.poke(true)

      program.zipWithIndex.foreach {
        case (instruction, address) =>
          uut.io.address.poke(address.U)
          uut.io.rw.write.data.poke(instruction)
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

      //should run?
      uut.clock.step(20)
    }
  }

}
