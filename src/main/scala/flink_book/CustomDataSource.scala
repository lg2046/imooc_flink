package flink_book

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.{ProcessWindowFunction, WindowFunction}
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.windows.{GlobalWindow, TimeWindow, Window}
import org.apache.flink.util.Collector

object CustomDataSource {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val hostname = if (params.has("hostname")) params.get("hostname") else "localhost"
    val port = if (params.has("port")) params.getInt("port") else 9999

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)

    //SourceFunction非并发数据源
    env.fromElements(1, 2, 3, 4, 5, 6).keyBy(v => 1).countWindow(3).process(new ProcessWindowFunction[Int, Int, Int, GlobalWindow] {
      override def process(key: Int, context: Context, elements: Iterable[Int], out: Collector[Int]): Unit = {
        for (e <- elements) {
          print(e)
        }
        out.collect(1000)
      }
    }).addSink(new RichSinkFunction[Int] {

      override def open(parameters: Configuration): Unit = {
        println(s"init a sink")
      }


      override def close(): Unit = {
        println("close a sink")
      }

      override def invoke(value: Int, context: SinkFunction.Context[_]): Unit = {
        println(value)
      }
    })

    env.execute("customDataSource")
  }
}
