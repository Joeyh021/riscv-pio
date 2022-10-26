import chisel3.stage.ChiselStage

object Main extends App {
  val defaultArgs = Array("--target-dir", "gen/")
  (new ChiselStage).emitVerilog(new axi.AXILiteRegisterSlave, defaultArgs ++ args)
}
