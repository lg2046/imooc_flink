import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._

object Parameter {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //可以dashboard在查看该参数
    env.getConfig.setGlobalJobParameters(params)

    //参数检测 在client端运行
    if (params.has("input")) {
      println(params.get("input"))
    }


    val dStream = env
      .socketTextStream("localhost", 9999)
      .flatMap(_.split("\\s+"))

    dStream.print()

    env.execute("MapString")
  }
}
