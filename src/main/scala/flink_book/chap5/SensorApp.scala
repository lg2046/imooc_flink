package flink_book.chap5

import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.runtime.state.KeyedBackendSerializationProxy
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.functions.{KeyedProcessFunction, ProcessFunction}
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

case class SensorReading(id: String, timestamp: Long, temperature: Double)

object SensorApp {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(2)

    env
      .addSource(new SensorSource)
      .assignTimestampsAndWatermarks(
        new BoundedOutOfOrdernessTimestampExtractor[SensorReading](Time.seconds(5)) {
          override def extractTimestamp(r: SensorReading): Long = r.timestamp
        }
      )
      //转成华氏度
      .map(r =>
        SensorReading(r.id, r.timestamp, (r.temperature - 32) / (5.0 / 9.0))
      )
      .keyBy(_.id)
      //1秒内不停上升就报警
      //      .process(new KeyedProcessFunction[String, SensorReading, String]() {
      //        //当前key的状态
      //        lazy val lastTemp: ValueState[Double] = getRuntimeContext.getState(
      //          new ValueStateDescriptor[Double]("lastTemp", Types.of[Double]))
      //
      //        lazy val currentTimer: ValueState[Long] = getRuntimeContext.getState(
      //          new ValueStateDescriptor[Long]("timer", Types.of[Long]))
      //
      //        override def processElement(r: SensorReading,
      //                                    ctx: KeyedProcessFunction[String, SensorReading, String]#Context,
      //                                    out: Collector[String]): Unit = {
      //          val prevTemp = lastTemp.value()
      //          lastTemp.update(r.temperature)
      //
      //          val curTimerTimestamp = currentTimer.value()
      //
      //          //温度下降了
      //          if (prevTemp == 0.0 || r.temperature < prevTemp) {
      //
      //            ctx.timerService().deleteProcessingTimeTimer(curTimerTimestamp)
      //            //温度上升 且还没有timer 注册1秒后的timer
      //          } else if (r.temperature > prevTemp && curTimerTimestamp == 0) {
      //            val timerTs = ctx.timerService().currentProcessingTime() + 1000
      //            ctx.timerService().registerProcessingTimeTimer(timerTs)
      //            currentTimer.update(timerTs)
      //          }
      //
      //        }
      //
      //        //timer触发时 调用onTimer, 与processElement 是synchronized的
      //        override def onTimer(timestamp: Long,
      //                             ctx: KeyedProcessFunction[String, SensorReading, String]#OnTimerContext,
      //                             out: Collector[String]): Unit = {
      //          out.collect(s"Temperature of sensor '${ctx.getCurrentKey}' monotonically increased for 1 second")
      //          currentTimer.clear()
      //        }
      //      })
      .timeWindow(Time.seconds(5))
      //计算平均温度
      .process(new ProcessWindowFunction[SensorReading, SensorReading, String, TimeWindow] {
        override def process(sensorId: String,
                             context: Context,
                             elements: Iterable[SensorReading],
                             out: Collector[SensorReading]): Unit = {
          val temps = elements.map(_.temperature)
          val avgTemp = temps.sum / temps.size

          out.collect(SensorReading(sensorId, context.window.getEnd, avgTemp))
        }
      })
      .print()

    env.execute("Compute average sensor temperature")
  }
}
