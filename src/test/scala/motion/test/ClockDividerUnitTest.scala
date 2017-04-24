package motion



import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


class ClockDividerUnitTester(d: ClockDivider) extends PeekPokeTester(d) {

  val pulseDivisor = 55
  val counter = 1
  poke(d.io.pulseEveryNCycles, pulseDivisor)

  for(i <- 0 until 20000){
    step(1)
    println("hehehj")
    expect(d.io.outputPulse, (i) % pulseDivisor  == 0)
  }
 
}


class ClockDividerTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    s"ClockDivider $backendName" should s"ClockDiv: $backendName" in {
      Driver(() => new ClockDivider, backendName) {
        c => new ClockDividerUnitTester(c)
      } should be (true)
    }
  }
}


