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


  // There is a somewhat slow state machine that deals with the
  // USB FIFO chip.  Since this is an asynchronous USB FIFO, I can't
  // guarantee that I can deal with handshaking in a single cycle.
  //
  // These two bits allow seperate, fast, state machines to deal with the
  // handshaking and to act as an input to the main state machine

  val outputConsumed = Reg(Bool())
  val inputFilled = Reg(Bool())

  // Set high when the output register is free.
  // Used to generate handshake to the rest of the FPGA.
  val inputRegisterFree = Reg(Bool())
  val outputRegisterFree = Reg(Bool())

  // This allows the idle state to swap between checking
  // to read from the computer and checking to write to the computer.
  // This ensures input and output are given equal priority and neither one
  // starves.  
  val check_read_next = Reg(Bool())


  io.inputReady := !rfx
  io.outputReady := !txe

  when (reset === true.B){
    state := sIdle
    inputRegisterFree := true.B
    outputRegisterFree := true.B
    check_read_next := true.B
  }.otherwise{

    // This is the main state machine.
    //
    // Roughly it does this:
    /*
     * Idle {
     * if read -> read_state
     *  else if write -> write_state
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

      when(check_read_next & inputFilled & !io.txe){
        //Trigger writting a byte to the host computer
        state := write_to_computer_start
        check_read_next := false.B
      }.elsewhen(outputRegisterFree & !io.rxf){
        state := read_from_computer
        check_read_next := true.B
      }.otherwise{
        check_read_next := !check_read_next
        state := sIdle
      }

    }.elsewhen(state === read_from_computer){

    }.elsewhen(state === write_to_computer_start){


      // We are running a 50Mhz clock, which has a 20ns period
      // Waiting one clock cycle is enough for the setup time.
      //
      //
      state := write_to_computer_wait

      // Drive the output on the I/O line.  This enables a tristate
      // buffer in connected to the I/O pin on the FPGA.
      io.enableOutput := 1.B

    }.elsewhen(state === write_to_computer_wait
      enableOutput := 1.B
      state := write_to_computer_strobe
    }.elsewhen(state === write_to_computer_strobe){
      
    }.otherwise{
      state := sIdle
    }
  }

}

