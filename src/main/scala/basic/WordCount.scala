package basic

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.AllWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.util.Collector

object WordCount {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)

    //    val checkpointPath = "file:///Users/cdliguang/per_doc/code/flink_learning/tmp"
    //    val backend = new FsStateBackend(checkpointPath, false)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)
    //    env.setStateBackend(backend)

    val dStream = env
      .socketTextStream("localhost", 9999)
      //      .assignTimestampsAndWatermarks()
      .map(_.toInt)


    val pattern = Pattern.begin[Int]("first")
      .followedBy("second")
      .followedBy("third")
      .where { (third, ctx) =>
        ctx.getEventsForPattern("first").head + ctx.getEventsForPattern("second").head == third
      }
      .within(Time.seconds(3))

    val patternStream = CEP.pattern(dStream, pattern)

    //      .countWindowAll(5)
    //      .apply(new CustomWindowFunction)

    //      .flatMap(_.split("\\s+").zip(Stream.continually(1)))
    //      .keyBy(0)
    //      .map(new RollingSum)
    //对每条消息计算该key历史总数


    //      .mapWithState[(String, Long), Long] { (in: (String, Int), sum: Option[Long]) =>
    //      sum match {
    //        case Some(v) => ((in._1, in._2 + v), Some(in._2 + v))
    //        case None => ((in._1, in._2), Some(in._2))
    //      }
    patternStream.select { p =>
      List(p("first").head, p("second").head, p("third").head)
    }.print()

    env.execute("word count")
  }
}

class RollingSum extends RichMapFunction[(String, Int), (String, Long)] {
  var sumState: ValueState[Long] = _

  override def open(parameters: Configuration): Unit = {
    val stateDescriptor = new ValueStateDescriptor[Long]("sumState", createTypeInformation[Long])
    sumState = getRuntimeContext.getState(stateDescriptor)
  }

  override def map(v: (String, Int)): (String, Long) = {
    val sumValue = sumState.value()
    val newSum = Option(sumValue).getOrElse(0L) + v._2
    sumState.update(newSum)

    (v._1, newSum)
  }
}

class CustomWindowFunction extends AllWindowFunction[Int, Int, GlobalWindow] {
  override def apply(window: GlobalWindow, input: Iterable[Int], out: Collector[Int]): Unit = {
    out.collect(input.sum * 2)
  }
}

//
//class RollingPartitionSum extends MapFunction[String, (String, Int)] with ListCheckpointed[Integer] {
//  var sum = 0
//
//  override def map(value: String): (String, Int) = {
//    sum += 1
//    (value, sum)
//  }
//
//  override def snapshotState(checkpointId: Long, timestamp: Long): java.util.List[Integer] = {
//    Collections.singletonList(sum)
//  }
//
//  override def restoreState(state: java.util.List[Integer]): Unit = {
//    for (s <- state.asScala) {
//      sum += s
//    }
//  }
//}

//CheckpointedFunction interface是指定stateful function的低级接口
//提供了用来注册与维护keyed state 和 operator state的hook
//定义了两个方法；
//    initializeState
//    snapshotState 在checkpoint前调用，并接受一个FunctionInitializationContext
//      FunctionInitializationContext可以访问checkpoint的唯一标识和JobManager初始化checkpoint的时间戳
//      该函数的目的为了保证在checkpoint前所有state object已更新
//该类并未保存自动checkpoint功能, 必须与CheckpointListener结合，这样snapshotState
//便可以一致性将数据写到外部存储系统
//class RollingRums3
//  extends MapFunction[(String, Int), (String, Int, Int)]
//    with CheckpointedFunction {
//
//  var keyedSumState: ValueState[Int] = _
//  var opSumState: ListState[Int] = _
//
//  override def map(value: (String, Int)): (String, Int, Int) = {
//    val keySum = Option(keyedSumState.value()).getOrElse(0) + value._2
//    val opSum = opSumState.get().asScala.sum + value._2
//    keyedSumState.update(keySum)
//    opSumState.clear()
//    opSumState.add(opSum)
//
//    (value._1, keySum, opSum)
//  }
//
//  override def snapshotState(context: FunctionSnapshotContext): Unit = ???
//
//  //在启动或者恢复时调用
//  override def initializeState(context: FunctionInitializationContext): Unit = {
//    val keyedSumDescriptor = new ValueStateDescriptor[Int](
//      "keyedSum",
//      createTypeInformation[Int]
//    )
//    keyedSumState = context.getKeyedStateStore.getState(keyedSumDescriptor)
//
//    val opSumDescriptor = new ListStateDescriptor[Int](
//      "opSum",
//      createTypeInformation[Int]
//    )
//    opSumState = context.getOperatorStateStore.getListState(opSumDescriptor)
//  }
//}

//模拟sessionWindow 30秒没有收到数据即清除, 但是用于单个元素，不像window后面必须接聚集函数
class RunningSumWithTimeout extends ProcessFunction[(String, Int), (String, Int)] {

  var sumState: ValueState[Int] = _
  var lastTimerState: ValueState[Long] = _


  override def open(parameters: Configuration): Unit = {
    val sumDescriptor: ValueStateDescriptor[Int] = new ValueStateDescriptor[Int]("sumState", createTypeInformation[Int])
    sumState = getRuntimeContext.getState(sumDescriptor)

    val timestampDescriptor: ValueStateDescriptor[Long] = new ValueStateDescriptor[Long]("timestampState", createTypeInformation[Long])
    lastTimerState = getRuntimeContext.getState(timestampDescriptor)
  }

  //30秒没有收到数据清空
  override def processElement(value: (String, Int),
                              ctx: ProcessFunction[(String, Int), (String, Int)]#Context,
                              out: Collector[(String, Int)]): Unit = {
    val checkTimestamp = System.currentTimeMillis() + (10 * 1000)
    val lastTimer = lastTimerState.value()

    //注册timer 在checkTimestamp时间点调用本operator的onTimer 方法
    if (Option(lastTimer).isEmpty || checkTimestamp > lastTimer) {
      ctx.timerService().registerProcessingTimeTimer(checkTimestamp)
      lastTimerState.update(checkTimestamp)
    }

    val sumValue = sumState.value()
    val newSum = Option(sumValue).getOrElse(0) + value._2
    sumState.update(newSum)
    out.collect((value._1, newSum))
  }

  override def onTimer(timestamp: Long,
                       ctx: ProcessFunction[(String, Int), (String, Int)]#OnTimerContext,
                       out: Collector[(String, Int)]): Unit = {

    val lastTimer = lastTimerState.value()
    if (Option(lastTimer).isDefined && lastTimer == timestamp) {
      sumState.clear()
      lastTimerState.clear()
    }
  }
}