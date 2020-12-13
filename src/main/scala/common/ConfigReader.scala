package common

import akka.actor.ActorSystem

trait ConfigReader {

  def mqttHost(implicit system: ActorSystem): String =
    system.settings.config.getString("http.mqtthost")

  def mqttPort(implicit system: ActorSystem): String =
    system.settings.config.getString("http.mqttport")

  def mqttUsername(implicit system: ActorSystem): String =
    system.settings.config.getString("http.mqttuser")

  def mqttPassword(implicit system: ActorSystem): String =
    system.settings.config.getString("http.mqttpwd")

  def camHost(implicit system: ActorSystem): String =
    system.settings.config.getString("http.camhost")

  def camPort(implicit system: ActorSystem): String =
    system.settings.config.getString("http.camport")

}

object ConfigReader extends ConfigReader
