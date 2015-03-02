package com.zqh.akka.WatchService

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import com.ning.http.client.{AsyncCompletionHandler, AsyncHttpClient, Response}

import scala.collection.mutable

/**
 * @author Tomasz Nurkiewicz
 * @since 20.05.12, 14:12
 */
case class FetchFromRandomOrg(batchSize: Int)

case class RandomOrgServerResponse(randomNumbers: List[Int])

class RandomOrgClient extends Actor {

	val client = new AsyncHttpClient()
	val waitingForReply = new mutable.Queue[(ActorRef, Int)]

	override def postStop() {
		client.close()
	}

	def receive = LoggingReceive {
		case FetchFromRandomOrg(batchSize) =>
			waitingForReply += (sender -> batchSize)
			if (waitingForReply.tail.isEmpty) {
				sendHttpRequest(batchSize)
			}
		case response: RandomOrgServerResponse =>
			waitingForReply.dequeue()._1 ! response
			if (!waitingForReply.isEmpty) {
				sendHttpRequest(waitingForReply.front._2)
			}
	}

	private def sendHttpRequest(batchSize: Int) {
		val url = "https://www.random.org/integers/?num=" + batchSize + "&min=0&max=65535&col=1&base=10&format=plain&rnd=new"
		client.prepareGet(url).execute(new RandomOrgResponseHandler(self))
	}
}

private class RandomOrgResponseHandler(notifyActor: ActorRef) extends AsyncCompletionHandler[Unit]() {
	def onCompleted(response: Response) {
		val numbers = response.getResponseBody.lines.map(_.toInt).toList
		notifyActor ! RandomOrgServerResponse(numbers)
	}
}