package imooc

import java.text.SimpleDateFormat

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object WaterMarkDemo {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val hostname = if (params.has("hostname")) params.get("hostname") else "localhost"
    val port = if (params.has("port")) params.getInt("port") else 9999

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val input = env.socketTextStream(hostname, port)

    input.map { f =>
      val arr = f.split("\\W+")
      (arr(0), arr(1).toLong)
    }
//      .assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[(String, Long)] {
//        //用于跟踪收到的元素中的最大的时间 一般是从元素中取的时间
//        var currentMaxTimestamp = 0L
//        //容忍等待时间
//        val maxOutOfOrderness = 10000L
//
//        var a: Watermark = _
//        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
//
//        //operator启动时就在不停的调用这个方法
//        override def getCurrentWatermark: Watermark = {
//          a = new Watermark(currentMaxTimestamp - maxOutOfOrderness)
//          a
//        }
//
//        //进来一条消息，取出它的时间
//        override def extractTimestamp(t: (String, Long), previousElementTimestamp: Long): Long = {
//          val timestamp = t._2
//          currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp)
//          println(a)
//          //println("timestamp:" + t._1 + "," + t._2 + "|" + format.format(t._2) + "," + currentMaxTimestamp + "|" + format.format(currentMaxTimestamp) + "," + a.toString)
//          timestamp
//        }
//      })
      .keyBy(_._1)
      .timeWindow(Time.seconds(3))
      .max(1)
//      .process(new ProcessWindowFunction[(String, Long), (String, Int, String, String, String, String), String, TimeWindow] {
//        override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[(String, Int, String, String, String, String)]): Unit = {
//          val list = elements.toList.sortBy(_._2)
//          val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
//          out.collect((key, list.size, format.format(list.head._2), format.format(list.last._2), format.format(context.window.getStart), format.format(context.window.getEnd)))
//        }
//      })
      .print()

    env.execute("watermark")
  }
}
