package com.hsbc.mods.finance.service

import akka.actor.{Actor, ActorLogging}
import akka.event.Logging
import akka.kafka.ConsumerMessage.CommittableMessage
import com.google.gson.Gson
import com.hsbc.mods.finance.models.Models.{EnrichedFinanceException, FinanceException}
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy

/**
  * Created by Bala on 3/21/17.
  */
class ExceptionEnrichmentActor extends Actor with ActorLogging {

  val logger = Logging.getLogger(this)

  override def preStart() = logger.info("The ExceptionEnrichment Actor is ready to receive the requests")

  override def postStop() = logger.info("The ExceptionEnrichment Actor is gonna stop and would not entertain any requests")

  def receive: Receive = {

    case financeRecordFromKafka:FinanceException => {
      val senderRef = sender()
      val enrichedFinanceException:EnrichedFinanceException = EnrichedFinanceException(financeRecordFromKafka.componentName, financeRecordFromKafka.sourceFunction, financeRecordFromKafka.exceptionAttribute, financeRecordFromKafka.uniqueKeyIdentifier, financeRecordFromKafka.uniqueKeyInstanceId,
        financeRecordFromKafka.autoAction, financeRecordFromKafka.exceptionCategory, financeRecordFromKafka.exceptionType, financeRecordFromKafka.exceptionSeverity, "SLA0002", "SLA Manager raised a new exception")
      senderRef ! enrichedFinanceException
    }
    case other =>
      sender() ! "Hey, Wrong information being sent to me!!"

  }

}
