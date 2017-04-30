// See LICENSE for license details.

package motion

import chisel3._
import chisel3.util.Enum

class Fifo extends Module {
  val io = IO(new Bundle {

    
    // These are to the rest of the FPGA
    val inputReady = Output(Bool())
    val outputReady = Input(Bool())

    // When this is high, the fifo will read dataIn and send it
    // to the host computer over USB.
    val fifoRead = Input(Bool())

    // When this is high, you *must* read from dataOut.
    // This will only be high when outputReady is asserted.
    // This gets strobed high for each byte as it comes off of the USB FIFO.
    val fifoWrite = Output(Bool())


    val dataIn     = Input(UInt(8.W))
    val dataOut    = Output(UInt(8.W))


    val rfx = Input(Bool())
    val read = Output(Bool())

    val txe = Input(Bool())
    val write = Output(Bool())

    val enableOutput = Output(Bool())
  })

  val sIdle :: readLoadData :: readStrobe :: writeLoadData :: writeStrobe
    :: checkWrite :: Nil = Enum(UInt(), 5)

  // The input register is the input to this module and the data which gets
  // written to the computer. 
  val inputRegister = Reg(UInt(8.W))

  // The output register was written to this module from the computer
  // the gets written to the rest of the FPGA.
  val outputRegister = Reg(UInt(8.W))

  val inputRegisterFree = Reg(Bool())
  val outputRegisterFree = Reg(Bool())

  // There is a somewhat slow state machine that deals with the
  // USB FIFO chip.  Since this is an asynchronous USB FIFO, I can't
  // guarantee that I can deal with handshaking in a single cycle.
  //
  // These two bits allow seperate, fast, state machines to deal with the
  // handshaking and to act as an input to the main state machine

  val outputConsumed = Reg(Bool())
  val inputFilled = Reg(Bool())



  io.inputReady := !rfx
  io.outputReady := !txe

  when (reset === true.B){
    state := sIdle
    inputRegisterFree := true.B
    outputRegisterFree := true.B
  }.otherwise{

    // This is the main state machine.
    //
    // Roughly it does this:
    /*
     * Idle {
     * if read -> read_state
     *  else if write -> write_state
     *  else check_handshake
     * }
     *  
     * read_state -> check if read_register is filled.
     *   if so, write out to the host computer.
     *   Now flag the input register as free
     * 
     * write_state -> check if the consumer can take a byte
     *   if so, let them have it!
     *   now flag the output register as free
     * 
     */

    when(state === sIdle){
      when(inputRegisterFree & io.rfx){
        //Trigger a read.
        state := readStrobe
      }.elsewhen(!inputRegisterFree & inputFilled
    }.elsewhen(state === readStrobe){
        state := checkWrite
      }.elsewhen(state === checkWrite){

        when(outputRegisterFree & io.txe){
        state := writeStrobe
      }.otherwise{
        state := sIdle
      }
    }.elsewhen(state === writeStrobe){

    }
  }
}

