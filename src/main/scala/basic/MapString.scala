package basic

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object MapString {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val dStream = env
      .socketTextStream("localhost", 9999)
      .flatMap(_.split("\\s+"))

    dStream.print()

    env.execute("MapString")
  }
}
