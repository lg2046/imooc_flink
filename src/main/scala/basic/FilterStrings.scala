package basic

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object FilterStrings {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream = env
      .socketTextStream("localhost", 9999) //创建初始的data source
      .filter(isDouble _) // filter transform 生成新stream

    dataStream.print()

    //stream计算均是惰性
    //启动流计算
    env.execute("FilterString Strings") // jobName
  }

  def isDouble(str: String): Boolean = {
    try {
      str.toDouble
      true
    } catch {
      case _: Throwable => false
    }
  }
}
