import java.util.Collections

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.{CheckpointedFunction, ListCheckpointed}
import org.apache.flink.streaming.api.scala._

import scala.collection.JavaConverters._
import org.apache.flink.runtime.state.filesystem.FsStateBackend

object WordCount {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)

    val checkpointPath = "hdfs://file/dir"
    val backend = new FsStateBackend(checkpointPath, false)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)
    env.setStateBackend(backend)

    val dStream = env
      .socketTextStream("localhost", 9999)
      .flatMap(_.split("\\s+").map(str => (str, 1)))
      .uid("flatMap-1")
      .keyBy(a => a._1)
      .map(new RollingRums3)
      .uid("map-roll-1")

    //      .mapWithState[(String, Long), Long] { (in: (String, Int), sum: Option[Long]) =>
    //      sum match {
    //        case Some(v) => ((in._1, in._2 + v), Some(in._2 + v))
    //        case None => ((in._1, in._2), Some(in._2))
    //      }
    dStream.print()

    env.execute("word count")
  }
}

//class RollingSum extends RichMapFunction[(String, Int), (String, Long)] {
//  var sumState: ValueState[Long] = _
//
//  override def open(parameters: Configuration): Unit = {
//    val stateDescriptor = new ValueStateDescriptor[Long]("sumState", createTypeInformation[Long])
//    sumState = getRuntimeContext.getState(stateDescriptor)
//  }
//
//  override def map(v: (String, Int)): (String, Long) = {
//    val sumValue = sumState.value()
//    val newSum = Option(sumValue).getOrElse(0L) + v._2
//    sumState.update(newSum)
//
//    (v._1, newSum)
//  }
//}
//
class RollingPartitionSum extends MapFunction[String, (String, Int)] with ListCheckpointed[Integer] {
  var sum = 0

  override def map(value: String): (String, Int) = {
    sum += 1
    (value, sum)
  }

  override def snapshotState(checkpointId: Long, timestamp: Long): java.util.List[Integer] = {
    Collections.singletonList(sum)
  }

  override def restoreState(state: java.util.List[Integer]): Unit = {
    for (s <- state.asScala) {
      sum += s
    }
  }
}

//CheckpointedFunction interface是指定stateful function的低级接口
//提供了用来注册与维护keyed state 和 operator state的hook
//定义了两个方法；
//    initializeState
//    snapshotState 在checkpoint前调用，并接受一个FunctionInitializationContext
//      FunctionInitializationContext可以访问checkpoint的唯一标识和JobManager初始化checkpoint的时间戳
//      该函数的目的为了保证在checkpoint前所有state object已更新
//该类并未保存自动checkpoint功能, 必须与CheckpointListener结合，这样snapshotState
//便可以一致性将数据写到外部存储系统
class RollingRums3
  extends MapFunction[(String, Int), (String, Int, Int)]
    with CheckpointedFunction {

  var keyedSumState: ValueState[Int] = _
  var opSumState: ListState[Int] = _

  override def map(value: (String, Int)): (String, Int, Int) = {
    val keySum = Option(keyedSumState.value()).getOrElse(0) + value._2
    val opSum = opSumState.get().asScala.sum + value._2
    keyedSumState.update(keySum)
    opSumState.clear()
    opSumState.add(opSum)

    (value._1, keySum, opSum)
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = ???

  //在启动或者恢复时调用
  override def initializeState(context: FunctionInitializationContext): Unit = {
    val keyedSumDescriptor = new ValueStateDescriptor[Int](
      "keyedSum",
      createTypeInformation[Int]
    )
    keyedSumState = context.getKeyedStateStore.getState(keyedSumDescriptor)

    val opSumDescriptor = new ListStateDescriptor[Int](
      "opSum",
      createTypeInformation[Int]
    )
    opSumState = context.getOperatorStateStore.getListState(opSumDescriptor)
  }
}

