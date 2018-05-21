import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._

object WordCount {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)

    val dStream = env
      .socketTextStream("localhost", 9999)
      .flatMap(_.split("\\s+").map(str => (str, 1)))
      .keyBy(0) //转成KeyedStream     0为tuple的索引
      .sum(1) //对KeyedStream进行操作  1为tuple的索引 其他agg函数有max min

    dStream.print() //每过来一个只会打印单独过来一条数据时所影响的数据

    env.execute("word count")
  }
}
