// See LICENSE for license details.

package motion

import chisel3._
import chisel3.util.Enum

import scala.language.reflectiveCalls

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


    val rxf = Input(Bool())
    val read = Output(Bool())

    val txe = Input(Bool())
    val write = Output(Bool())

    val enableOutput = Output(Bool())
  })

  // These are the enumerations for the outer state maching
  val sIdle :: sReadFromComputer :: sWriteToComputer :: Nil = Enum(3)

  val state = Reg(UInt(3.W))

  val (sWriteIdle :: sWriteStart :: sWriteToComputerWait0 ::
      sWriteToComputerWait1 :: sWriteToComputerStrobe :: Nil) = Enum(5)

  val writeState = Reg(UInt(3.W))

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

  // This is the logic which drives the main state machine back to the
  // idle state.
  val returnMainStateMachineToIdle = false.B


  io.inputReady := !io.rxf
  io.outputReady := !io.txe

  when (reset === true.B){
    state := sIdle
    inputRegisterFree := true.B
    outputRegisterFree := true.B
    check_read_next := true.B
  }.otherwise{

    // This is the main state machine.
    //
    // Roughly it does this:

    when(state === sIdle){

      when(check_read_next & inputFilled & !io.txe){
        //Trigger writting a byte to the host computer
        state := sWriteToComputer
        check_read_next := false.B
      }.elsewhen(outputRegisterFree & !io.rxf){
        state := sReadFromComputer
        check_read_next := true.B
      }.otherwise{
        check_read_next := !check_read_next
        state := sIdle
      }
    }.otherwise {
      when(returnMainStateMachineToIdle){
        state := sIdle
      }.otherwise{
        state := state
      }
    }
  }

  val writeStrobeCounter = Reg(UInt(4.W))


  when(reset === true.B){
    io.enableOutput := false.B
  }otherwise{
    when(writeState === sWriteIdle){
      io.enableOutput := true.B
      writeStrobeCounter := 4.U

      when(state === sWriteToComputer){
        writeState := sWriteStart
      }.otherwise{
        writeState := sWriteIdle
      }

     }.elsewhen(writeState === sWriteStart){


      // We are running a 50Mhz clock, which has a 20ns period
      // Waiting one clock cycle is enough for the setup time.
      //
      // I'm going to wait twice so that I don't have to fuss
      // with glitches.
      writeState := sWriteToComputerWait0

      // Drive the output on the I/O line.  This enables a tristate
      // buffer in connected to the I/O pin on the FPGA.
      io.enableOutput := true.B

      writeStrobeCounter := writeStrobeCounter

    }.elsewhen(writeState === sWriteToComputerWait0){
      io.enableOutput := true.B
      state := sWriteToComputerWait0
      writeStrobeCounter := writeStrobeCounter
    }.elsewhen(writeState === sWriteToComputerWait1){
      io.enableOutput := true.B
      state := sWriteToComputerStrobe

    }.elsewhen(state === sWriteToComputerStrobe){
      io.enableOutput := true.B
      writeStrobeCounter := writeStrobeCounter - 1.U
      when(writeStrobeCounter === 0.U ){
        writeState := sWriteIdle
      }.otherwise{
        writeState := sWriteToComputerStrobe
      }
    }.otherwise{
      state := sIdle
    }
  }
}








