package flink_book.chap5

import org.apache.flink.streaming.api.functions.source.RichSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext

import scala.util.Random

class SensorSource extends RichSourceFunction[SensorReading] {
  var running = true

  override def run(ctx: SourceContext[SensorReading]): Unit = {
    val rand = new Random()

    val taskIdx = getRuntimeContext.getIndexOfThisSubtask

    //每个subTask 都是 初始化2个sensor 并初始化初始温度
    var curFTemp = (1 to 2).map { i =>
      ("sensor_" + (taskIdx * 10 + i), 65 + (rand.nextGaussian() * 20))
    }

    //每100ms生成一批数据
    while (running) {
      curFTemp = curFTemp.map(t => (t._1, t._2 + (rand.nextGaussian() * 0.5)))
      val curTime = System.currentTimeMillis()
      curFTemp.foreach { t =>
        ctx.collect(SensorReading(t._1, curTime, t._2))
      }

      Thread.sleep(100)
    }
  }

  override def cancel(): Unit = running = false
}
