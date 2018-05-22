package basic

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._

case class VisitTime(pageId: String, visitTime: Double, c: Int)

object AverageVisitTime {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)

    val dStream = env
      .socketTextStream("localhost", 9999)
      .map { str =>
        val strs = str.split("\\s+")
        VisitTime(strs(0), strs(1).toDouble, 1)
      } //拆成[page_id, 10seconds, 1] 用于求平均的分子与分母
      .keyBy(0) //转成KeyedStream     0为tuple的索引
      .reduce { (t1, t2) => VisitTime(t1.pageId, t1.visitTime + t2.visitTime, t1.c + t2.c) }
      .map { p => (p.pageId, p.visitTime / p.c) }

    dStream.print()

    env.execute("AverageTime")
  }
}
