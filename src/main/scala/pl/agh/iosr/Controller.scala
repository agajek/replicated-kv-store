package pl.agh.iosr

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.actor.Actor.Receive
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.http.scaladsl.server.PathMatchers.Segment
import pl.agh.iosr.KVStore.{Get, Put}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import pl.agh.iosr.Controller.Start

import scala.concurrent.duration._

class Controller(kVStore: ActorRef) extends Actor with Json4sSupport {

  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val serialization = jackson.Serialization
  implicit val format = DefaultFormats
  implicit val timeout = Timeout(5 seconds)


  override def preStart(): Unit = {
    Http().bindAndHandle(route, "localhost", 8080)
  }

  val route =
    path("store" / Segment / "value" / IntNumber) { (key: String, value: Int) =>
      put {
        kVStore ! Put(key, value)
        complete(s"Inserted $value at key $key")
      }
    } ~
    path("store" / Segment) { key: String =>
      get {
        onComplete((kVStore ? Get(key)).mapTo[Int]) { r =>
          complete(r)
        }
      }
    }



  override def receive: Receive = {

    case Start =>
  }
}

object Controller {

  def props(kvStore: ActorRef): Props = Props(new Controller(kvStore))

  def singletonManagerProps(controllerProps: Props)(implicit system: ActorSystem): Props =
    ClusterSingletonManager.props(
      singletonProps = controllerProps,
      terminationMessage = PoisonPill,
      ClusterSingletonManagerSettings(system)
    )

  def singletonProxyProps(implicit system: ActorSystem): Props =
    ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/$singletonManagerName",
      settings = ClusterSingletonProxySettings(system)
    )

  val singletonManagerName = "ControllerManager"

  case object Start
}
