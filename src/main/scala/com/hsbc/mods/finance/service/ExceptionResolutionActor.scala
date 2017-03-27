package com.hsbc.mods.finance.service

import akka.actor.{Actor, ActorLogging}
import akka.event.Logging
import com.hsbc.mods.finance.models.Models.EnrichedFinanceException
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy

/**
  * Created by Bala on 3/21/17.
  */
class ExceptionResolutionActor extends Actor with ActorLogging {

  val logger = Logging.getLogger(this)

  override def preStart() = logger.info("The ExceptionResolution Actor is ready to receive the requests")

  override def postStop() = logger.info("The ExceptionResolution Actor is gonna stop and would not entertain any requests")

  def receive: Receive = {

    case enrichedFinanceException:EnrichedFinanceException =>
      sender() ! enrichedFinanceException
    case other =>
      sender() ! "Hey, Wrong information being sent to me!!"

  }

}
