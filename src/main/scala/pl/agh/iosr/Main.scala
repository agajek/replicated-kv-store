package pl.agh.iosr

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.amazonaws.auth.InstanceProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.ec2.AmazonEC2Client
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import pl.agh.iosr.infrastructure.EC2
import scala.collection.JavaConversions._

object Main extends App {
  private lazy val ec2 = {
    val credentials = new InstanceProfileCredentialsProvider
    val region = Region.getRegion(Regions.EU_CENTRAL_1)
    val scalingClient = new AmazonAutoScalingClient(credentials) { setRegion(region) }
    val ec2Client = new AmazonEC2Client(credentials) { setRegion(region) }
    new EC2(scalingClient, ec2Client)
  }

  val (host, siblings, port) = (ec2.currentIp, ec2.siblingIps, "2551")

  println(siblings + "ssssssssssssssssss")
  val seeds = siblings map (ip => s"akka.tcp://ClusterSystem@$ip:2551")

  private val overrideConfig =
    ConfigFactory.empty()
      .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(host))
      .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(port))
      .withValue("akka.cluster.seed-nodes", ConfigValueFactory.fromIterable(seeds))

  private val defaults = ConfigFactory.load()

  val config = overrideConfig withFallback defaults

  implicit val system = ActorSystem("ClusterSystem", config)
  implicit val mat = ActorMaterializer()

  val kVStore = system.actorOf(KVStore.props, "KVStore")

  val controller = new Controller(kVStore, system)

  Http().bindAndHandle(controller.route, "localhost", config.getInt("http.port"))
}
