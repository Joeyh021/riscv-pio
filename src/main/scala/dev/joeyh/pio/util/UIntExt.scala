package dev.joeyh.pio.util

import chisel3._

object UIntExt {
  implicit class SAddImplicit(val a: UInt) {
    def /+\(b: UInt): UInt = 32.U.min(a + b)
  }
}
