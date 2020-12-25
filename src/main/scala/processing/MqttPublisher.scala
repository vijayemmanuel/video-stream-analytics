package processing

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.mqtt
import akka.stream.alpakka.mqtt.scaladsl.{ MqttSink, MqttSource }
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions }
import akka.stream.scaladsl.{ Keep, Sink, Source }
import akka.util.ByteString
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import transform.WithGrey

import scala.concurrent.Future

/**
 * Created by Vijay on 20/11/20.
 */
object MqttPublisher {
}

class MqttPublisher(host: String, port: String, user: String, pwd: String) {
  val connectionSettings = MqttConnectionSettings(
    s"tcp://$host:$port",
    "tcp-client",
    new MemoryPersistence
  ).withAuth(s"$user", s"$pwd")

  /**
   * Given a detected faces , publish to MQTT
   */
  def publish(system: ActorSystem, grey: WithGrey, faces: Seq[Classified]): (WithGrey, Seq[Classified]) = {
    implicit val ec = system
    val mqttSource: Source[MqttMessage, Future[Done]] =
      MqttSource.atMostOnce(
        connectionSettings,
        MqttSubscriptions(Map("face" -> MqttQoS.AtLeastOnce)),
        bufferSize = 10
      )
    val mqttSink: Sink[MqttMessage, Future[Done]] =
      MqttSink(connectionSettings, MqttQoS.AtLeastOnce)

    if (faces.length != 0) {
      // Trigger MQTT message only if trigger is passed
      import system.dispatcher
      //Source.single(MqttMessage.create("face", ByteString("FACE_DETECTED"))).runWith(mqttSink)

      mqttSource.take(1).toMat(Sink.seq)(Keep.both).run()._2.map {
        x =>
          if (x.head.payload.utf8String == "READY")
            Source.single(MqttMessage.create("face", ByteString("FACE_DETECTED"))).runWith(mqttSink)
      }
    }
    (grey, faces)
  }

}
