import chisel3.stage.ChiselStage

object Main extends App {
  val defaultArgs =
    Array("--target-dir", "gen/", "--emission-options=disableMemRandomization,disableRegisterRandomization")
  (new ChiselStage).emitVerilog(new blinky.BlinkAxiLite, defaultArgs ++ args)
}
