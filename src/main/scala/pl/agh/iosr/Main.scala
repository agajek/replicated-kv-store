package pl.agh.iosr

import akka.actor.ActorSystem
import pl.agh.iosr.Controller._


object Main extends App {

  implicit val system = ActorSystem("ClusterSystem")

  val kVStore = system.actorOf(KVStore.props, "KVStore")

  system.actorOf(singletonManagerProps(Controller.props(kVStore)), singletonManagerName)
  val controller =  system.actorOf(singletonProxyProps, "Controller")

  controller ! Start
}
