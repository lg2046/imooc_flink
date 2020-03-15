package flink_book

import java.util
import java.util.Collections

import org.apache.flink.api.common.ExecutionConfig
import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.api.common.typeutils.TypeSerializer
import org.apache.flink.streaming.api.environment
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.{ProcessingTimeTrigger, Trigger}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object SocketWordCount {
  def main(args: Array[String]): Unit = {
    //val params = ParameterTool.fromArgs(args)
    //val hostname = if (params.has("hostname")) params.get("hostname") else "localhost"
    //val port = if (params.has("port")) params.getInt("port") else 9999

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)

    //    val text = env.socketTextStream(hostname, port, '\n')
    //    val text = env.readTextFile("data/input.txt")
    //
    //    val windowCount = text
    //    .flatMap { w => w.split("\\s+") }
    //      .map { w => WordWithCount(w, 1) }
    //      .keyBy("word")
    //      .timeWindow(Time.seconds(5))
    //      .sum("count")

    //    windowCount.print()

    val ds2 = env.fromElements(1, 2, 3)

    env.addSource(new SourceFunction[Integer] {
      var running: Boolean = true

      override def run(ctx: SourceFunction.SourceContext[Integer]): Unit = {
        var start = 1
        while (running) {
          ctx.collect(start)
          Thread.sleep(500)
          start += 1
        }
      }

      override def cancel(): Unit = running = false
    })
      .keyBy(_ % 2 == 0)
      .intervalJoin(ds2.keyBy(_ % 2 == 0))
      .between(Time.minutes(-1), Time.minutes(1))
      .process(new ProcessJoinFunction[Integer, Int, Int] {
        override def processElement(left: Integer, right: Int, ctx: ProcessJoinFunction[Integer, Int, Int]#Context, out: Collector[Int]): Unit =
          out.collect(left + right)
      })
      //      .window(new WindowAssigner[Object, TimeWindow] {
      //        //窗口大小3秒
      //        val windowSize: Long = 3 * 1000L
      //
      //        override def assignWindows(o: Object, ts: Long,
      //                                   ctx: WindowAssigner.WindowAssignerContext): util.Collection[TimeWindow] = {
      //          val now = ctx.getCurrentProcessingTime
      //          val startTime = now - now % windowSize
      //          val endTime = startTime + windowSize
      //
      //          println(startTime, endTime)
      //          Collections.singletonList(new TimeWindow(startTime, endTime))
      //        }
      //
      //        override def getDefaultTrigger(env: environment.StreamExecutionEnvironment): Trigger[Object, TimeWindow] =
      //          ProcessingTimeTrigger.create()
      //
      //        override def getWindowSerializer(executionConfig: ExecutionConfig): TypeSerializer[TimeWindow] =
      //          new TimeWindow.Serializer
      //
      //        override def isEventTime: Boolean = false
      //      }).reduce((a, b) => new Integer(a + b))
      .print()

    env.execute("Socket Window WordCount")
  }

  case class WordWithCount(word: String, count: Long)

}
