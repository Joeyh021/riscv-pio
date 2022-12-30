package dev.joeyh.pio.execution

import chisel3._
import chisel3.util._

class DecodeIO extends Bundle {}

class Decode extends Module {
  val io = IO(new DecodeIO)
}
