name := "finance_aem_poc"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= {

  val akkaStreamKafkaVersion = "0.14"
  val typeSafeConfigVersion = "1.3.1"

  Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion,
    "com.typesafe" % "config" % typeSafeConfigVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-http" % "5.2.11",
    "com.sksamuel.elastic4s" %% "elastic4s-tcp" % "5.2.11",
    "com.sksamuel.elastic4s" %% "elastic4s-core" % "5.2.11",
    "com.sksamuel.elastic4s" %% "elastic4s-jackson" % "5.2.11",
    "com.google.code.gson" % "gson" % "2.8.0"
  )
}
    