//package demo
//
//import com.sksamuel.elastic4s.ElasticDsl._
////import scala.concurrent.ExecutionContext.Implicits.global
//
//object EsDemo {
//  def main(args: Array[String]): Unit = {
//
//    import utils.EsClient
//
//    //    EsClient.cluster_name = "jiesi-102"
//    //    EsClient.connectStr = "172.20.130.120:9310,172.20.133.58:9310,172.20.133.59:9310,172.20.138.154:9310"
//    //    EsClient.reconnect()
//
//    val client = EsClient.getClient
//
//    val orderIds = List(
//      64506833949L,
//      64513346908L,
//      64514094428L,
//      64514135388L,
//      64060945266L,
//      63803833116L,
//      64060849554L,
//      64499578750L,
//      64462537119L,
//      64505774269L,
//      64513259804L,
//      63751595807L
//    )
//    orderIds.foreach { orderId =>
//      client.execute {
//        search("global_v2" / "order_2016") query {
//          termQuery("orderId", orderId)
//        }
//      }.await.getHits.getHits.map(_.getSource).foreach { source =>
//        println(List("name", "pin", "orderId", "identityId").map(source.get(_)))
//      }
//    }
//
//
//    //根据identity反查订单
//    val identityIds = List(
//      "215c32369c16a96d51dd0585aab42239",
//      "3c897e38254cee7efe303f9979e96537"
//    )
//    identityIds.foreach { identityId =>
//      client.execute {
//        search("global_v2" / "order_2016") query {
//          termQuery("identityId", identityId)
//        }
//      }.await.getHits.getHits.map(_.getSource).foreach { source =>
//        println(List("name", "pin", "orderId", "identityId").map(source.get(_)))
//      }
//    }
//  }
//}