package pl.agh.iosr

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import pl.agh.iosr.KVStore.{Get, Put}

import scala.concurrent.duration._

class Controller(kVStore: ActorRef, system: ActorSystem) extends Json4sSupport {

  implicit val serialization = jackson.Serialization
  implicit val format = DefaultFormats
  implicit val timeout = Timeout(5 seconds)

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
}