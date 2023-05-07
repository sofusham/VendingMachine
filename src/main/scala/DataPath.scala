import chisel3._
import chisel3.util._

class DataPath extends Module {
  val io = IO(new Bundle {
    val price = Input(UInt(5.W))
    val coin2 = Input(Bool())
    val coin5 = Input(Bool())
    val bust = Input(Bool())
    val downcount = Input(Bool())
    val enough = Output(Bool())
    val sum = Output(UInt(8.W))
    val empty = Output(Bool())
    val canOut = Output(UInt(5.W))
  })

  val sum = RegInit(0.U(8.W))
  val canCount = RegInit(24.U(5.W))

  //edge detection coin2 og coin5
  val moent2 = io.coin2 && !(RegNext(io.coin2, false.B))
  val moent5 = io.coin5 && !(RegNext(io.coin5, false.B))
  io.empty := 0.B

  // system to add the amount inserted to the balance
  when(moent2) {
    sum := sum + 2.U
  }.elsewhen(moent5) {
    sum := sum + 5.U
  }

  //system to subtract the price from the balance once a can is bought
  when(io.downcount) {
    sum := sum - io.price
  }

  //system to determine whether a purchase is allowed
  io.enough := io.price <= sum

  //system to count the amount of cans
  when(canCount > 1.U) {
    io.empty := 1.B
  }
  when(io.downcount) {
    canCount := canCount - 1.U
    } .elsewhen (io.bust) {
    canCount := 24.U
  }

  io.empty := canCount === 0.U
  io.sum := sum

  //can counter
  when(io.downcount) {
    canCount := canCount - 1.U
  }
  io.canOut := canCount



}