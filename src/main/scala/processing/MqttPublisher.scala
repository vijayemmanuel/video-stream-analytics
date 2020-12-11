package processing

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.mqtt.scaladsl.MqttSink
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttMessage, MqttQoS }
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.ByteString
import common.Dimensions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import transform.WithGrey

import scala.concurrent.Future

/**
 * Created by Vijay on 20/11/20.
 */
object MqttPublisher {
}

class MqttPublisher() {
  val connectionSettings = MqttConnectionSettings(
    "tcp://192.168.1.88:1883",
    "tcp-client",
    new MemoryPersistence
  ).withAuth("mqtt", "mqtt")

  /**
   * Given a detected faces , publish to MQTT
   */
  def publish(system: ActorSystem, grey: WithGrey, faces: Seq[Classified]): (WithGrey, Seq[Classified]) = {
    implicit val ec = system
    val sink: Sink[MqttMessage, Future[Done]] =
      MqttSink(connectionSettings, MqttQoS.AtLeastOnce)
    if (faces.length != 0) {
      Source.single(MqttMessage.create("face", ByteString("FACE DETECTED"))).runWith(sink)
    }
    (grey, faces)
  }

}
