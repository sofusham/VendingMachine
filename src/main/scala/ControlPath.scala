import chisel3._
import chisel3.util._

class ControlPath extends Module {
  val io = IO(new Bundle {
    val buy = Input(Bool())
    val enough = Input(Bool())
    val empty = Input(Bool())
    val alarm = Output(Bool())
    val releaseCan = Output(Bool())
    val downcount = Output(Bool())
  })

  //initialization for somes wires
  io.downcount := 0.B
  io.alarm := 0.B
  io.releaseCan := 0.B


  //mechanism for the state machine itself
  val StateReg = RegInit(0.U(2.W))

  switch(StateReg) {
    is(0.U) {
      io.releaseCan := 0.B
      io.alarm := 0.B
      io.downcount := 0.B
      when(!io.buy && io.enough || !io.buy && !io.enough) {
        StateReg := 0.U
      }.elsewhen(io.buy && (!io.enough || io.empty)) {
        StateReg := 1.U
      }.elsewhen(io.buy && io.enough) {
        StateReg := 2.U
      }
    }
    is(1.U) {
      io.releaseCan := 0.B
      io.alarm := 1.B
      io.downcount := 0.B
      when(io.buy && (!io.enough || io.empty)) {
        StateReg := 1.U
      }.elsewhen(!io.buy && io.enough || !io.buy && !io.enough || io.buy && io.enough) {
        StateReg := 0.U
      }
    }
    is(2.U) {
      io.releaseCan := 1.B
      io.alarm := 0.B
      io.downcount := 0.B
      when(io.buy) {
        StateReg := 2.U
      }.elsewhen(!io.buy) {
        StateReg := 3.U
      }
    }
    is(3.U) {
      io.releaseCan := 0.B
      io.alarm := 0.B
      io.downcount := 1.B
      StateReg := 0.U
    }
  }
}