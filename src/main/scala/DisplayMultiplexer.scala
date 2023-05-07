import chisel3._
import chisel3.util._

class DisplayMultiplexer(maxCount: Int) extends Module {
  val io = IO(new Bundle {
    val sum = Input(UInt(8.W))
    val price = Input(UInt(8.W))
    val seg = Output(UInt(7.W))
    val an = Output(UInt(4.W))
  })

  val sevSeg = WireDefault("b1111111".U(7.W))
  val select = WireDefault("b0001".U(4.W))

  // *** your code starts here
  val cntSUS = RegInit(0.U(20.W))
  val cntDISPLAY = RegInit(0.U(3.W))
  val SUS_MAX = 100000.U
  val DISPLAY_MAX = 4.U

  val dec0 = WireDefault("b1111111".U(7.W))
  val dec1 = WireDefault("b1111111".U(7.W))
  val dec2 = WireDefault("b1111111".U(7.W))
  val dec3 = WireDefault("b1111111".U(7.W))

  val decoder0 = Module(new SevenSegDecoder())
  decoder0.io.sw := io.price(3,0)
  dec0 := decoder0.io.seg

  val decoder1 = Module(new SevenSegDecoder())
  decoder1.io.sw := io.price(7,4)
  dec1 := decoder1.io.seg

  val decoder2 = Module(new SevenSegDecoder())
  decoder2.io.sw := io.sum(3,0)
  dec2 := decoder2.io.seg

  val decoder3 = Module(new SevenSegDecoder())
  decoder3.io.sw := io.sum(7,4)
  dec3 := decoder3.io.seg

  //counters til hhv. at nå clockhastighed på 1kHz og skifte mellem displays
  cntSUS := cntSUS + 1.U
  when (cntSUS === SUS_MAX) {
    cntSUS := 0.U
    cntDISPLAY := cntDISPLAY + 1.U
  }
  when (cntDISPLAY === DISPLAY_MAX) {
    cntDISPLAY := 0.U
  }

  //skifter hvilket display der er tændt
  switch (cntDISPLAY) {
    is (0.U){
      select := "b0001".U
      sevSeg := dec0
    }
    is (1.U) {
      select := "b0010".U
      sevSeg := dec1
    }
    is (2.U) {
      select := "b0100".U
      sevSeg := dec2
    }
    is (3.U) {
      select := "b1000".U
      sevSeg := dec3
    }
  }

  // *** your code ends here

  io.seg := sevSeg
  io.an := ~select
}
