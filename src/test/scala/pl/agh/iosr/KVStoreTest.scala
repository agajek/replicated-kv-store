package pl.agh.iosr

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class KVStoreTest extends Simulation {

  val httpProtocol = http
    .baseURL("http://localhost:1080")

  val computerDbScn = scenario("Computer Scenario")
    .exec(http("Put value")
      .put("/store/a/value/1"))

    .exec(http("get value")
      .get("/store/a"))

  setUp(computerDbScn.inject(
    constantUsersPerSec(2) during(1 minute)
  ).protocols(httpProtocol))
}
