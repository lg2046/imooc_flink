package imooc

import imooc.utils.Resource._
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import scala.io.{BufferedSource, Source}

object CounterApp {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val hostname = if (params.has("hostname")) params.get("hostname") else "localhost"
    val port = if (params.has("port")) params.getInt("port") else 9999

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    env.registerCachedFile("file:///Volumes/data2/github/flink_playground/data/input.txt", "input.txt")

    val source = env.fromCollection(List(1, 2, 3, 4, 5))
    source.map(new RichMapFunction[Int, Int] {
      val counter = new LongCounter

      override def open(parameters: Configuration): Unit = {
        getRuntimeContext.addAccumulator("ele-counts", counter)

        val file = getRuntimeContext.getDistributedCache.getFile("input.txt")

        using(Source.fromFile(file))(_.getLines().foreach(println))
      }

      override def map(in: Int): Int = {
        counter.add(1)
        in
      }
    }).writeAsText("data/counter-out", WriteMode.OVERWRITE)

    val result = env.execute("CounterApp")
    println(result.getAccumulatorResult[Long]("ele-counts"))
  }
}
