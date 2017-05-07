// See LICENSE for license details.



package motion

import scala.language.reflectiveCalls
import chisel3._



class Stepper extends Module {
  val io = IO(new Bundle {
    val enable = Input(Bool());
    val motorPhases  = Output(UInt(4.W))
  })

  val stepState  = Reg(UInt())


  when(reset){
    stepState := 0.U;
    io.motorPhases := 0.U;
  }.elsewhen(io.enable){
    when(stepState === 0.U){
      stepState := 1.U;
      io.motorPhases := 12.U
    }.elsewhen(stepState === 1.U){
      stepState := 2.U;
      io.motorPhases := 6.U;
    }.elsewhen(stepState === 2.U){
      stepState := 3.U;
      io.motorPhases := 3.U;
    }.elsewhen(stepState === 3.U){
      stepState := 0.U;
      io.motorPhases := 9.U;
    }.otherwise {
      stepState := 0.U;
      io.motorPhases := 0.U;
    }
  }
}

