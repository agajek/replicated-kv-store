package pl.agh.iosr

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import pl.agh.iosr.KVStore._

class KVStore extends Actor {
  import akka.cluster.ddata.Replicator._

  val replicator = DistributedData(context.system).replicator
  implicit val cluster = Cluster(context.system)

  def dataKey(key: String): LWWMapKey[Int] = LWWMapKey(key)

  override def receive: Receive = {
    case Put(key, value) =>
      replicator ! Update(dataKey(key), LWWMap.empty[Int], WriteLocal)(_ + (key -> value))

    case KVStore.Delete(key) =>
      replicator ! Update(dataKey(key), LWWMap(), WriteLocal)(_ - key)

    case KVStore.Get(key) =>
      replicator ! Get(dataKey(key), ReadLocal, Some(Request(key, sender())))

    case g @ GetSuccess(LWWMapKey(_), Some(Request(key, replyTo))) =>
      g.dataValue match {
        case data: LWWMap[_] =>
          replyTo ! data.get(key).get
      }
  }
}


object KVStore {

  def props: Props = Props(new KVStore)

  case class Put(key: String, value: Int)
  case class Get(key: String)
  case class Delete(key: String)

  private final case class Request(key: String, replyTo: ActorRef)

}