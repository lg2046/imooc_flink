//package demo
//
//import java.net.URLDecoder
//
//import com.jd.bdp.jdq.config.ENV
//import com.jd.bdp.jdq.control.JDQ_ENV
//import kafka.serializer.StringDecoder
//import org.apache.spark.sql.SparkSession
//import org.apache.spark.streaming.kafka.KafkaUtils
//import org.apache.spark.streaming.{Durations, StreamingContext}
//import utils.JimdbClientStatic
//
//class TopicDemo {}
//
//object TopicDemo {
//  def main(args: Array[String]): Unit = {
//    JDQ_ENV.assignRunningEnv(ENV.ONLINE)
//
//    val spark = SparkSession.builder()
//      .appName("TopicDemo")
//      .enableHiveSupport()
//      .config("spark.streaming.backpressure.enabled", value = true)
//      .getOrCreate()
//
//    val ssc = new StreamingContext(spark.sparkContext, Durations.seconds(5))
//
//    // 部署在JRC上使用的checkpoint目录  只在使用了窗口操作的时候再打开
//    val checkpointDirectory = "hdfs://galaxy/user/spark/checkpoint/wm-streaming-TopicDemo"
//    ssc.checkpoint(checkpointDirectory)
//
//    val appId = "ufs.jd.com"
//    val token = "V50jaEojRYPGF45l4J4mk1p3oFUyb6sBmbXanyxGoss="
//    val topicName = "www.100000"
//
//    val kafkaParams = Map(
//      "appId" -> appId,
//      "token" -> token,
//      "spark.jdq.offset.reset" -> "current",
//      "spark.jdq.offset.auto" -> "false",
//      "topic.partition.subconcurrency" -> "3"
//    )
//
//    val topic = Set(topicName)
//
//    val stream = KafkaUtils.createJDQDirectStreamWithOffset[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topic)
//
//    stream.foreachRDD { rdd =>
//      //业务处理 at least once, 如果需要exactly once需要保证幂等性
//      rdd.map { msg =>
//        msg._2.split("\t")
//      }.filter { fields =>
//        //只取生鲜首页
//        val ctUrl = URLDecoder.decode(fields(7), "utf-8")
//        val trimUrl = ctUrl.replaceFirst("^(http://www\\.|http://|www\\.|https://www\\.|https://)", "")
//        trimUrl.startsWith("fresh.jd.com")
//      }.foreachPartition { iter =>
//        JimdbClientStatic().incrBy("fresh_pv", iter.size)
//      }
//
//      //手动提交位点
//      KafkaUtils.commitOffset(appId, token, rdd)
//    }
//
//    ssc.start()
//    ssc.awaitTermination()
//  }
//}
//
//
//
