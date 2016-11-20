package pl.agh.iosr

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory


object Main extends App {

  implicit val system = ActorSystem("ClusterSystem")
  implicit val mat = ActorMaterializer()
  val config = ConfigFactory.load()

  val kVStore = system.actorOf(KVStore.props, "KVStore")

  val controller = new Controller(kVStore, system)

  Http().bindAndHandle(controller.route, "localhost", config.getInt("http.port"))
}
