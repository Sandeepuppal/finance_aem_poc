package com.hsbc.mods.finance.service

import akka.actor.{Actor, ActorLogging}
import akka.event.Logging
import com.hsbc.mods.finance.models.Models.EnrichedFinanceException
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import com.sksamuel.elastic4s.TcpClient
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import com.sksamuel.elastic4s.ElasticDsl.{indexInto, _}

/**
  * Created by Bala on 3/21/17.
  */
class ESPersistenceActor(client: TcpClient) extends Actor with ActorLogging {

  val logger = Logging.getLogger(this)

  override def preStart() = logger.info("The ESPersistence Actor is ready to receive the requests")

  override def postStop() = logger.info("The ESPersistence Actor is gonna stop and would not entertain any requests")

  def receive: Receive = {

    case enrichedFinanceException:EnrichedFinanceException =>
      client.execute {
        indexInto("finance" / "portfolio") doc (enrichedFinanceException) refresh (RefreshPolicy.IMMEDIATE)
      }
      sender() ! enrichedFinanceException
    case other =>
      sender() ! "Hey, Wrong information being sent to me!!"

  }

}
