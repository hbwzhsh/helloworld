package com.zqh.akka.cleakka.test

import com.zqh.akka.cleakka.DirectByteBufferCleaner

object Benchmark {
  private val LOOP_COUNT = 1000000
  private val ARRAY_SIZE = 1024

  def main(args: Array[String]) {
    val t1 = System.currentTimeMillis

    var i = 0
    while (i < LOOP_COUNT) {
      testDirectByteBuffer()
      i += 1
    }
    val t2 = System.currentTimeMillis
    println("" + (t2 - t1) + " [ms]")
  }

  private def testDirectByteBuffer() {
    val b = java.nio.ByteBuffer.allocateDirect(ARRAY_SIZE)
    b.put(new Array[Byte](ARRAY_SIZE))
    DirectByteBufferCleaner.clean(b)
  }
}
