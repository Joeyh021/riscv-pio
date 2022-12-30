package dev.joeyh
import chisel3.stage.ChiselStage

object Main extends App {
  val defaultArgs      = Array("--target-dir", "gen/")
  val neaterOutputArgs = Array("--emission-options=disableMemRandomization,disableRegisterRandomization")

  (new ChiselStage).emitVerilog(new pio.PIO, defaultArgs)
}
