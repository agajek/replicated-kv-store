package pl.agh.iosr

import akka.actor.ActorSystem
import pl.agh.iosr.Controller.Start


object Main extends App {

  implicit val system = ActorSystem("ClusterSystem")

  val kVStore = system.actorOf(KVStore.props, "KVStore")
  val controller = system.actorOf(Controller.props(kVStore), "Controller")

  controller ! Start
}
