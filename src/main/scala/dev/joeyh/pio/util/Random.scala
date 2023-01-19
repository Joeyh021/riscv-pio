package dev.joeyh.pio.util

import chisel3._
import chisel3.util._

object RandomUInt {
  def apply(width: Int): UInt = scala.util.Random.between(0, math.pow(2, width)).toInt.U
}

object RandomBool {
  def apply(): Bool = (scala.math.random() < 0.5).B
}
