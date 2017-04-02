// See LICENSE.txt for license details.
package problems

import chisel3._
// import Counter._


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


  val counter = Reg(UInt(width=16.W))

  when(counter === io.pulseEveryNCycles){
    counter := 0.asUInt(16.W);
  }.otherwise{
    counter := counter + 1.asUInt(16.W);
  }

}
