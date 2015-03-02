package com.zqh.akka.cleakka

import java.net.URLEncoder
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{
  Actor, ActorRef, Props,
  ActorSelection, Identify, ActorIdentity
}
import akka.actor.ActorDSL._
import akka.pattern.ask

object CacheServer {
  // TODO: Support scala.concurrent.duration.Duration

  sealed trait Msg

  case class  IsDefinedAt(key: Any)                               extends Msg
  case class  Put        (key: Any, value: Any, ttlSecs: Int = 0) extends Msg
  case class  PutIfAbsent(key: Any, value: Any, ttlSecs: Int = 0) extends Msg
  case class  Get        (key: Any)                               extends Msg
  case class  Remove     (key: Any)                               extends Msg
  case object RemoveAll                                           extends Msg
  case object GetStats                                            extends Msg

  //----------------------------------------------------------------------------

  def start(cacheName: String, limitInMB: Int): ActorRef = {
    ActSys.SYSTEM.actorOf(Props(classOf[CacheServer], limitInMB), escapeActorName(cacheName))
  }

  def connect(cacheName: String): Future[Option[ActorRef]] = {
    val path = "/user/" + escapeActorName(cacheName)
    val sel  = ActSys.SYSTEM.actorSelection(path)
    actorSelection2ActorRef(sel)
  }

  def connect(cacheName: String, host: String, port: Int): Future[Option[ActorRef]] = {
    val path = "akka://" + ActSys.NAME + "@" + host + ":" + port + "/user/" + escapeActorName(cacheName)
    val sel  = ActSys.SYSTEM.actorSelection(path)
    actorSelection2ActorRef(sel)
  }

  private def escapeActorName(cacheName: String) = URLEncoder.encode(cacheName, "UTF-8")

  private def actorSelection2ActorRef(sel: ActorSelection): Future[Option[ActorRef]] = {
    implicit val timeout = ActSys.TIMEOUT
    sel.ask(new Identify(None)).mapTo[ActorIdentity].map(_.ref)
  }
}

/** An actor that wraps Cache. */
class CacheServer(limitInMB: Int) extends Actor {
  import CacheServer._

  private[this] val cache = new Cache(limitInMB)

  override def postStop() {
    cache.removeAll()
  }

  def receive = {
    case IsDefinedAt(key) =>
      sender ! cache.isDefinedAt(key)

    case Put(key, value, ttlSecs) =>
      cache.put(key, value, ttlSecs)

    case PutIfAbsent(key, value, ttlSecs) =>
      sender ! cache.putIfAbsent(key, value, ttlSecs)

    case Get(key) =>
      sender ! cache.get(key)

    case Remove(key) =>
      sender ! cache.remove(key)

    case RemoveAll =>
      cache.removeAll()

    case GetStats =>
      sender ! cache.stats

    case _ =>
  }
}
