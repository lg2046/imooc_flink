//import org.apache.flink.api.common.functions.Partitioner
//import org.apache.flink.streaming.api.TimeCharacteristic
//import org.apache.flink.streaming.api.functions.source.SourceFunction
//import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
//
//case class SensorReading(
//                          id: String,
//                          timestamp: Long,
//                          temperature: Double
//                        )
//
//class SensorSource extends SourceFunction[SensorReading] {
//  override def run(ctx: SourceFunction.SourceContext[SensorReading]): Unit = ???
//
//  override def cancel(): Unit = ???
//}
//
//object SensorDemo {
//  def main(args: Array[String]): Unit = {
//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//
//    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
//
//    val sensorData = env
//      .addSource(new SensorSource)
//      .setParallelism(4)
//      //      .assignTimestampsAndWatermarks(new SensorTimeAssigner)
//      .keyBy(1)
//      .partitionCustom(myPartitioner, "timestamp")
//
//  }
//}
//
//
//object myPartitioner extends Partitioner[Int] {
//  private val r = scala.util.Random
//
//  override def partition(key: Int, numPartitions: Int): Int = {
//    if (key < 0) 0 else r.nextInt(numPartitions)
//  }
//}