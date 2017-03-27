package com.hsbc.mods.finance

import java.time.Instant

import akka.actor.{ActorSystem, Props}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.hsbc.mods.finance.service.FinanceActor
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Bala on 3/20/17.
  */
object FinanceMain extends App {

  val config = ConfigFactory.load()
  implicit val system = ActorSystem.create("akka-stream-kafka-consumer-mods-program", config)
  implicit val mat = ActorMaterializer()
  val client = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))

  implicit val financeActor = system.actorOf(Props(new FinanceActor(client)).withRouter(FromConfig()), name = "financeActor")

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("mods-akka-stream-kafka-test")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  Consumer.committableSource(consumerSettings, Subscriptions.topics("test"))
    .map(msg => {
      financeActor ! msg
      println(msg)
    })
    .runWith(Sink.ignore)

  // prevent WakeupException on quick restarts of vm
  scala.sys.addShutdownHook {
    println("Terminating... - " + Instant.now)
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
    println("Terminated... Bye - " + Instant.now)
  }

}