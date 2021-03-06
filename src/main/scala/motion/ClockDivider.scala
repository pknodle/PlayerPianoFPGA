// See LICENSE.txt for license details.
package motion

import chisel3._
// import Counter._
import scala.language.reflectiveCalls

// This module outputs a single high going pulse on outputPulse every N
// clock cycles as governed by the input pulseEveryNCycles
//
// This allows you to time events at a slower rate then the system
// clock while keeping a single clock domain.

class ClockDivider extends Module {
  val io = IO(new Bundle {
    val pulseEveryNCycles = Input(UInt(16.W))
    val outputPulse = Output(Bool())
  })


  val counter = RegInit(UInt(width=16.W), 0.U(16.W))


  when(counter === (io.pulseEveryNCycles - 1.U(16.W))){
    counter := 0.asUInt(16.W);
    io.outputPulse := true.B
  }.otherwise{
    counter := counter + 1.asUInt(16.W);
    io.outputPulse := false.B
  }

}
