package dev.joeyh.pio.fifo

import chisel3._
import chisel3.util._

//An asynchronous 4x32bit FIFO
//allows for read/writing in different clock domains
//https://github.com/chipsalliance/rocket-chip/blob/master/src/main/scala/util/AsyncQueue.scala

class AsyncFifoIO extends Bundle {
  val producerClock = Input(Clock())
  val producerReset = Input(Bool())
  val producer      = new ProducerIO

  val consumerClock = Input(Clock())
  val consumerReset = Input(Bool())
  val consumer      = new ConsumerIO
}

class Fifo extends RawModule {
  val io = IO(new AsyncFifoIO)

  val readAddress  = Wire(UInt(2.W))
  val writeAddress = Wire(UInt(2.W))
  val readPointer  = Wire(UInt(3.W))
  val writePointer = Wire(UInt(3.W))

  //write clock domain (producer)
  withClockAndReset(io.producerClock, io.producerReset) {
    val write              = Module(new WritePointer)
    val readPointerCrossed = SyncPointer(readPointer)
    io.producer.full := write.io.full
    writeAddress := write.io.address
    writePointer := write.io.pointer
    write.io.readPointerCrossed := SyncPointer(readPointer)
    write.io.doWrite := io.producer.doWrite
  }

  //read clock domain (consumer)
  withClockAndReset(io.consumerClock, io.consumerReset) {
    val read = Module(new ReadPointer)

    io.consumer.empty := read.io.empty
    readAddress := read.io.address
    readPointer := read.io.pointer
    read.io.writePointerCrossed := SyncPointer(writePointer)
    read.io.doRead := io.consumer.doRead
  }

}

class ReadPointer extends Module {
  val io = IO(new Bundle {
    val empty               = Output(Bool())
    val address             = Output(UInt(2.W))
    val pointer             = Output(UInt(3.W))
    val writePointerCrossed = Input(UInt(3.W))
    val doRead              = Input(Bool())
  })

  val readPointer = RegInit(0.U(3.W)) //gray-coded read pointer
  readPointer := readGrayNext
  val readBinary = RegInit(0.U(3.W)) //binary-coded read pointer
  readBinary := readBinaryNext

  io.address := readBinary(1, 0)

  val readBinaryNext = readBinary + (io.doRead && !empty) //increment if told to and not empty
  val readGrayNext   = (readBinaryNext >> 1) ^ readBinaryNext

  //is the fifo empty?
  val empty = RegInit(true.B) //fifo is empty on reset
  empty := (readGrayNext === io.writePointerCrossed)
  io.empty := empty
}

class WritePointer extends Module {
  val io = IO(new Bundle {
    val full               = Output(Bool())
    val address            = Output(UInt(2.W))
    val pointer            = Output(UInt(3.W))
    val readPointerCrossed = Input(UInt(3.W))
    val doWrite            = Input(Bool())
  })

  val writePointer = RegInit(0.U(3.W)) //gray-coded write pointer
  writePointer := writeGrayNext
  val writeBinary = RegInit(0.U(3.W)) //binary-coded write pointer
  writeBinary := writeBinaryNext

  io.address := writeBinary(1, 0)

  val writeBinaryNext = writeBinary + (io.doWrite && !full) //increment if told to and not empty
  val writeGrayNext   = (writeBinaryNext >> 1) ^ writeBinaryNext

  //is the fifo full?
  val full = RegInit(false.B) //fifo is empty on reset

  //the three conditions we need for full
  full := ((writeGrayNext(2) =/= io.readPointerCrossed(2))
    && (writeGrayNext(1) =/= io.readPointerCrossed(1))
    && (writeGrayNext(0) === writeGrayNext(0)))

  io.full := full
}

//runs in the clock domain of the OUTPUT pointer
class SyncPointer extends Module {
  val io = IO(new Bundle {
    val syncedPointer = Output(UInt(3.W))
    val inputPointer  = Input(UInt(3.W))
  })

  //synchronise through a pair of registers
  val reg1 = RegInit(0.U)
  val reg2 = RegInit(0.U)

  io.syncedPointer := reg2
  reg2 := reg1
  reg1 := io.inputPointer
}

object SyncPointer {
  def apply(inputPointer: UInt) = {
    val m = Module(new SyncPointer)
    m.io.inputPointer := inputPointer
    m.io.syncedPointer
  }
}
