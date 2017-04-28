// See LICENSE for license details.

package motion

import chisel3._
import chisel3.util.Enum

class Fifo extends Module {
  val io = IO(new Bundle {
    val dataIn     = Input(UInt(8.W))
    val dataOut    = Output(UInt(8.W))

    val inputReady = Input(Bool())
    val outputReady = Input(Bool())

    val rfx = Input(Bool())
    val read = Output(Bool())

    val txe = Input(Bool())
    val write = Output(Bool())

    val enableOutput = Output(Bool())
  })

  val sIdle :: readLoadData :: readStrobe :: writeLoadData :: writeStrobe
  :: Nil = Enum(UInt(), 5)
  val wasLastOperationWrite = Bool()

  io.inputReady := !rfx
  io.outputReady := !txe

  when(state === sIdle){
    when(wasLastOperationWrite ){

    }.elsewhen(){

    }.otherwise{
      state := sIdle
    }
  }

}
