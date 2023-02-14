package dev.joeyh.pio.fifo

import chisel3._
import chisel3.util._

//An asynchronous 4x32bit FIFO
//allows for read/writing in different clock domains
//https://github.com/chipsalliance/rocket-chip/blob/master/src/main/scala/util/AsyncQueue.scala

class AsyncFifoIO extends Bundle {
  val producerClock = Input(Clock())
  val producerReset = Input(Reset())
  val producer      = new ProducerIO

  val consumerClock = Input(Clock())
  val consumerReset = Input(Reset())
  val consumer      = new ConsumerIO
}

class Fifo extends RawModule {
  val io = IO(new AsyncFifoIO)

  val readAddress  = Wire(UInt(2.W))
  val writeAddress = Wire(UInt(2.W))
  val readPointer  = Wire(UInt(3.W))
  val writePointer = Wire(UInt(3.W))

  //the actual fifo memory
  val mem = Mem(4, UInt(32.W))

  withClockAndReset(io.producerClock, io.producerReset) {
    val write = Module(new WritePointer)

    io.producer.full := write.io.full
    writeAddress := write.io.address
    writePointer := write.io.pointer
    write.io.readPointerCrossed := SyncPointer(readPointer)
    write.io.doWrite := io.producer.doWrite

    //write into the memory at writeAddress when producer signals a doWrite
    when(io.producer.doWrite) {
      mem.write(writeAddress, io.producer.write, io.producerClock)
    }
  }

  //read clock domain (consumer)
  withClockAndReset(io.consumerClock, io.consumerReset) {
    val read = Module(new ReadPointer)

    io.consumer.empty := read.io.empty
    readAddress := read.io.address
    readPointer := read.io.pointer
    read.io.writePointerCrossed := SyncPointer(writePointer)
    read.io.doRead := io.consumer.doRead

    io.consumer.read := mem.read(readAddress)
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

  val readBinaryNext = Wire(UInt(3.W))
  val readGrayNext   = Wire(UInt(3.W))

  val readPointer = RegInit(0.U(3.W)) //gray-coded read pointer
  readPointer := readGrayNext
  val readBinary = RegInit(0.U(3.W)) //binary-coded read pointer
  readBinary := readBinaryNext

  //is the fifo empty?
  val empty = RegInit(true.B) //fifo is empty on reset

  io.address := readBinary(1, 0)
  io.pointer := readPointer

  readBinaryNext := readBinary + (io.doRead && !empty) //increment if told to and not empty
  readGrayNext := (readBinaryNext >> 1) ^ readBinaryNext

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

  val writeBinaryNext = Wire(UInt(3.W))
  val writeGrayNext   = Wire(UInt(3.W))

  val writePointer = RegInit(0.U(3.W)) //gray-coded write pointer
  writePointer := writeGrayNext
  val writeBinary = RegInit(0.U(3.W)) //binary-coded write pointer
  writeBinary := writeBinaryNext

  io.address := writeBinary(1, 0)
  io.pointer := writePointer

  //is the fifo full?
  val full = RegInit(false.B) //fifo is empty on reset

  writeBinaryNext := writeBinary + (io.doWrite && !full) //increment if told to and not empty
  writeGrayNext := (writeBinaryNext >> 1) ^ writeBinaryNext

  //the three conditions we need for full
  full := ((writeGrayNext(2) =/= io.readPointerCrossed(2))
    && (writeGrayNext(1) =/= io.readPointerCrossed(1))
    && (writeGrayNext(0) === writeGrayNext(0)))

  io.full := full
}

object SyncPointer {
  def apply(inputPointer: UInt) = {
    //synchronise through a pair of registers
    val reg1 = RegInit(0.U)
    val reg2 = RegInit(0.U)
    reg1 := inputPointer
    reg2 := reg1
    reg2

  }
}
