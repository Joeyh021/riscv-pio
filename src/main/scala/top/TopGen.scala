package top

import chisel3.stage.ChiselStage

object TopGen extends App {
  val defaultArgs = Array("--target-dir", "gen/")
  (new ChiselStage).emitVerilog(new gcd.GCD, defaultArgs ++ args)
}
