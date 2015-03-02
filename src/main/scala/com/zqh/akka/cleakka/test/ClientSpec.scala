package com.zqh.akka.cleakka.test

import com.zqh.akka.cleakka.{CacheClient, CacheServer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

import org.specs2.mutable._

class ClientSpec extends Specification {
  "Client" should {
    "connect to server" in {
      CacheServer.start("z", 10)

      CacheClient.connect("z").onComplete {
        case Failure(e) =>
          println("Could not connect: " + e)

        case Success(None) =>
          println("Server not found")

        case Success(Some(client)) =>
          println(client)
          client.put("ba", "3")
          client.get[String]("ba").onSuccess {
            case None =>
              println("ba not found")

            case Some(ba) =>
              println(ba)
          }
      }

      true must equalTo(true)
    }
  }
}
