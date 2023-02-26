package dev.joeyh.pio

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.tags.Slow
import dev.joeyh.pio.util._
import chisel3.experimental.Analog

class PioWrapper extends Module {
  val io = IO(new Bundle {
    //address CSR and instructions in one 8-bit address space
    val address = Input(UInt(8.W))
    val rw      = new ReadWrite(UInt(16.W))

    val rx = new fifo.ConsumerIO
    val tx = new fifo.ProducerIO

    val pins = Analog(32.W)
  })
  val pio = Module(new PIO)
  pio.io.address <> io.address
  pio.io.rw <> io.rw
  pio.io.rx <> io.rx
  pio.io.tx <> io.tx
  pio.io.pins <> io.pins
  pio.io.pioClock := clock

}

@Slow
class PIOTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "PIO Block "

  it should "emit a square wave" in {
    test(new PioWrapper).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { uut =>
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
      uut.io.rw.write.data.poke(10)
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

      //pull enable high
      uut.io.address.poke(39)
      uut.io.rw.write.data.poke(1.U)
      uut.clock.step()
      uut.io.rw.write.enable.poke(false)

      //should run?
      //check the waves hahaha
      uut.clock.step(20)
    }
  }

  it should "output from x and y with wrap" in {
    test(new PioWrapper).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { uut =>
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

      //pull enable high
      uut.io.address.poke(39)
      uut.io.rw.write.data.poke(1.U)
      uut.clock.step()
      uut.io.rw.write.enable.poke(false)

      //should run?
      //check the waves hahaha
      uut.clock.step(20)
    }
  }

  it should "do the ws2812b thing" in {
    test(new PioWrapper).withAnnotations(Seq(VerilatorBackendAnnotation, WriteVcdAnnotation)) { uut =>
      //program
      val program = Seq(
        "b011_0_0010_001_00001".U, //shift 1 bit from OSR into X, side set 0, delay 2
        "b000_1_0001_001_00011".U, //branch on value of X (if x is zero then jump to 3, else fall through to 2), side 1 delay 1
        "b000_1_0100_000_00000".U, //jump back to 0, side 1 delay 4
        "b101_0_0100_101_00_101".U //nop, side 0, delay 4
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

      //wrap target needs to be set to 0, wrap trigger 3 (our loop)
      uut.io.address.poke(34)
      uut.io.rw.write.data.poke("b1_00000_00011_00000".U)
      uut.clock.step()

      //isr/osr config
      //set autopull to 24 bits, shift direction right
      uut.io.address.poke(38.U)
      uut.io.rw.write.data.poke("b_11000_1_1".U) //auto enabled with thresh 24, shift right
      uut.clock.step()

      //pin config
      //leave input and output as zero, only using side set pin

      //pull enable high
      uut.io.address.poke(39)
      uut.io.rw.write.data.poke(1.U)
      uut.clock.step()
      uut.io.rw.write.enable.poke(false)

      //should run!
      uut.clock.step(3)
      //should stall for some cycles
      //write data into fifo
      uut.io.tx.doWrite.poke(true)
      uut.io.tx.write.poke("b11111111_11111111_11111111".U)
      uut.clock.step()
      uut.io.tx.full.expect(false.B)
      uut.io.tx.doWrite.poke(false)
      uut.clock.step(100)
    }
  }

}
