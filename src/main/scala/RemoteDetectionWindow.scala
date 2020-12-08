import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.ConfigReader
import org.bytedeco.javacv.CanvasFrame
import org.slf4j.LoggerFactory
import processing.DetectMotion
import transform.{ Flip, MediaConversion }
import video.ImageProcessingSinks.ShowImageSink
import video.{ RPiCamWebInterface, Webcam }

object RemoteDetectionWindow extends App {

  val logger = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem()
  //implicit val materializer = ActorMaterializer()

  val canvas = new CanvasFrame("Webcam")
  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

  implicit val ec = system.dispatcher

  val remoteCameraSource = Webcam.remote(RPiCamWebInterface(ConfigReader.host))

  val graph = remoteCameraSource
    .map(
      _.map(Flip.horizontal)
        .grouped(2)
        .via(DetectMotion())
        .map(MediaConversion.matToFrame)
        .to(ShowImageSink(canvas))
    )

  graph.map(_.run())

}
