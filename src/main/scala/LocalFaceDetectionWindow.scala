
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.Dimensions
import modify.AnnotateDrawer
import org.bytedeco.javacv.CanvasFrame
import org.slf4j.LoggerFactory
import processing.{ FaceDetector, MqttPublisher }
import transform.{ Flip, MediaConversion, WithGrey }
import video.ImageProcessingSinks.ShowImageSink
import video.Webcam

object LocalFaceDetectionindow extends App {

  val logger = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem()
  //implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  val imageDimensions = Dimensions(width = 512, height = 288)
  val detector = FaceDetector.defaultCascadeFile(imageDimensions)
  val mqttPublisher = new MqttPublisher()

  val localCameraSource = Webcam.local(
    devicePath = "/Users/vijay/Downloads/WhatsAppVideo2019-11-23at18.36.19.mp4",
    dimensions = imageDimensions
  )

  val canvas = new CanvasFrame("Webcam")
  //Set Canvas frame to close on exit
  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

  val drawer = new AnnotateDrawer()

  val graph = localCameraSource
    .map(MediaConversion.frameToMat)
    .map(WithGrey.build)
    .map(detector.detect)
    .map(x => mqttPublisher.publish(system, x._1, x._2))
    .map(x => drawer.annotate(x._1, x._2, "face"))
    .map(MediaConversion.matToFrame) // convert back to a frame
    .to(ShowImageSink(canvas))

  graph.run()

}