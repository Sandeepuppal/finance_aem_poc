package com.hsbc.mods.finance.service

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import akka.event.Logging
import akka.kafka.ConsumerMessage.CommittableMessage
import com.google.gson.Gson
import com.hsbc.mods.finance.models.Models.{EnrichedFinanceException, FinanceException}
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import com.sksamuel.elastic4s.ElasticDsl.{indexInto, _}
import com.sksamuel.elastic4s.TcpClient
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Bala.
  */

object FinanceActor {
  def props(): Props = {
    Props(classOf[FinanceActor])
  }
}


class FinanceActor(client: TcpClient) extends Actor with ActorLogging {

  val logger = Logging.getLogger(this)
  val timeoutDuration = 10 seconds
  val actorTimeoutDuration = Timeout(timeoutDuration)

  override def preStart() = logger.info("The Finance Actor is ready to receive the requests")

  override def postStop() = logger.info("The Finance Actor is gonna stop and would not entertain any requests")

  override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30 seconds) {
    case _: Exception => Restart
  }


  val exceptionEnrichmentActor = context.actorOf(Props[ExceptionEnrichmentActor], "exceptionEnrichmentActor")
  lazy val eSPersistenceActor = context.actorOf(Props(classOf[ESPersistenceActor], client), "eSPersistenceActor")
  lazy val exceptionResolutionActor = context.actorOf(Props(classOf[ExceptionResolutionActor]), "exceptionResolutionActor")

  def receive: Receive = {

    case commited: CommittableMessage[k,v] => {
      val senderRef = sender()
      val gson = new Gson
      val financeRecordFromKafka:FinanceException = gson.fromJson(commited.record.value().toString, classOf[FinanceException])
      val enrichedFinanceExceptionResponse = exceptionEnrichmentActor.ask(financeRecordFromKafka)(actorTimeoutDuration)
      enrichedFinanceExceptionResponse.onSuccess {
        case enrichedFinanceException:EnrichedFinanceException => {
          val persistedFinanceExceptionResponse = eSPersistenceActor.ask(enrichedFinanceException)(actorTimeoutDuration)
          persistedFinanceExceptionResponse.onSuccess {
            case persistedFinanceException:EnrichedFinanceException => {
              val resolvedFinanceExceptionResponse =exceptionResolutionActor.ask(persistedFinanceException)(actorTimeoutDuration)
              resolvedFinanceExceptionResponse.onSuccess {
                case resolvedFinanceException:EnrichedFinanceException => {
                  println("Exception has been enriched, persisted and evaluated")
                  senderRef ! "Success"
                }
              }
              }
          }
        }

      }
      }
    case other =>
      sender() ! "Hey, Wrong information being sent to me!!"

  }

}
