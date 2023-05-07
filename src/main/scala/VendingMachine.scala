import chisel3._
import chisel3.util._

class VendingMachine(maxCount: Int) extends Module {
  val io = IO(new Bundle {
    val price = Input(UInt(5.W))
    val coin2 = Input(Bool())
    val coin5 = Input(Bool())
    val buy = Input(Bool())
    val bust = Input(Bool())
    val releaseCan = Output(Bool())
    val alarm = Output(Bool())
    val seg = Output(UInt(7.W))
    val an = Output(UInt(4.W))
    val empty = Output(Bool())
  })

  val sevSeg = WireDefault(0.U)
  // ***** some dummy connections *****
  sevSeg := "b1111111".U

  val DP = Module(new DataPath())
  DP.io.price := io.price
  DP.io.coin2 := io.coin2
  DP.io.coin5 := io.coin5
  DP.io.bust := io.bust
  io.empty := DP.io.empty

  val CP = Module(new ControlPath())
  CP.io.buy := io.buy
  io.alarm := CP.io.alarm
  io.releaseCan := CP.io.releaseCan

  CP.io.enough := DP.io.enough
  DP.io.downcount := CP.io.downcount
  CP.io.empty := DP.io.empty

  val bcd1 = Module(new BcdTable())
  bcd1.io.address := DP.io.sum
  val bcd2 = Module(new BcdTable())
  bcd2.io.address := DP.io.price
  val bcd3 = Module(new BcdTable())
  bcd3.io.address := DP.io.canOut

  val mux = Module(new DisplayMultiplexer(maxCount))
  io.seg := mux.io.seg
  io.an := mux.io.an
  mux.io.sum := bcd1.io.data
  mux.io.price := bcd2.io.data

  // display cans left and "bund"
  val amogus = RegInit(0.U(1.W))
  val imposter = RegInit(0.U(1.W))
  val counter = RegInit(0.U(28.W))

  //falling edge detection on release can
  when(!CP.io.releaseCan && (RegNext(CP.io.releaseCan, false.B))) {
    amogus := 1.U
  }
  when(amogus === 1.U) {
    //shows amount of cans left for 2 seconds
    counter := counter + 1.U
    mux.io.price := bcd3.io.data
    mux.io.sum := "b11001010".U
    when(counter === 200000000.U) {
      counter := 0.U
      amogus := 0.U
      mux.io.sum := bcd1.io.data
      mux.io.price := bcd2.io.data
    }
  }.otherwise {
    mux.io.sum := bcd1.io.data
    mux.io.price := bcd2.io.data
  }
  //Falling edge detection on amogus
  when(!amogus && (RegNext(amogus, false.B)) === 1.U) {
    imposter := 1.U
  }
  when(imposter === 1.U) {
    //shows bund for 1 second
    counter := counter + 1.U
    mux.io.sum := "b10111110".U
    mux.io.price := "b11111101".U
    when(counter === 100000000.U) {
      counter := 0.U
      imposter := 0.U
      mux.io.sum := bcd1.io.data
      mux.io.price := bcd2.io.data
    }
  }
}
  // generate Verilog
  object VendingMachine extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new VendingMachine(100000))
  }