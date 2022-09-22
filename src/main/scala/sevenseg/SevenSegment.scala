package sevenseg

import chisel3._
import chisel3.util._
import chisel3.experimental.VecLiterals._

class DisplayIO extends Bundle {
  val anodes   = Output(UInt(8.W))
  val segments = Output(UInt(7.W))
  val digits   = Input(Vec(8, UInt(4.W)))
}

class SevenSegment extends Module {
  val io = IO(new DisplayIO)

  val activeDigit       = RegInit(0.U(3.W))
  val (counterValue, _) = Counter(true.B, Math.pow(2, 20).toInt)

  activeDigit := counterValue(19, 17)

  //set one active anode
  io.anodes := MuxCase(
    0.U,
    Seq(
      (activeDigit === 0.U) -> "b01111111".U,
      (activeDigit === 1.U) -> "b10111111".U,
      (activeDigit === 2.U) -> "b11011111".U,
      (activeDigit === 3.U) -> "b11101111".U,
      (activeDigit === 4.U) -> "b11110111".U,
      (activeDigit === 5.U) -> "b01111011".U,
      (activeDigit === 6.U) -> "b01111101".U,
      (activeDigit === 7.U) -> "b11111110".U
    )
  )

  //select the correct input based on the current active digit
  val activeDigitValue = RegInit(0.U(4.W))
  activeDigitValue := io.digits(activeDigit)

  //select segment pattern
  io.segments := !MuxCase(
    "b1111111".U,
    Seq(
      (activeDigitValue === 0.U)  -> "b1111110".U,
      (activeDigitValue === 1.U)  -> "b0110000".U,
      (activeDigitValue === 2.U)  -> "b1101101".U,
      (activeDigitValue === 3.U)  -> "b1111001".U,
      (activeDigitValue === 4.U)  -> "b0110011".U,
      (activeDigitValue === 5.U)  -> "b1011011".U,
      (activeDigitValue === 6.U)  -> "b1011111".U,
      (activeDigitValue === 7.U)  -> "b1110000".U,
      (activeDigitValue === 8.U)  -> "b1111111".U,
      (activeDigitValue === 9.U)  -> "b1111011".U,
      (activeDigitValue === 10.U) -> "b1110111".U,
      (activeDigitValue === 11.U) -> "b0011111".U,
      (activeDigitValue === 12.U) -> "b1001110".U,
      (activeDigitValue === 13.U) -> "b0111101".U,
      (activeDigitValue === 14.U) -> "b1001111".U,
      (activeDigitValue === 15.U) -> "b1000111".U
    )
  )

}
