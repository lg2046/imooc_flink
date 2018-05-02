//package utils
//
//import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
//import org.elasticsearch.common.settings.Settings
//
//object EsClient {
//  var cluster_name = "jiesi-91"
//  var connectStr = "172.28.80.43:9316,172.28.80.45:9316,172.28.80.47:9316"
//
//  private var client: ElasticClient = _
//
//  private def connect(): Unit = {
//    try {
//      val settings = Settings.builder.put("cluster.name", cluster_name)
//        .put("client.transport.sniff", false).build
//
//      client = ElasticClient.transport(settings,
//        ElasticsearchClientUri(s"elasticsearch://${connectStr.replaceAll("\\s", "")}"))
//    }
//    catch {
//      case e: Exception => e.printStackTrace()
//    }
//  }
//
//  def getClient: ElasticClient = {
//    if (client == null) {
//      connect()
//    }
//    client
//  }
//
//  def reconnect(): Unit = connect()
//}
