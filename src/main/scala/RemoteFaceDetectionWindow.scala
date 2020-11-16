import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.{ ConfigReader, Dimensions }
import modify.FaceDrawer
import org.bytedeco.javacv.CanvasFrame
import org.slf4j.LoggerFactory
import processing.FaceDetector
import transform.{ MediaConversion, WithGrey }
import video.ImageProcessingSinks.ShowImageSink
import video.{ RPiCamWebInterface, Webcam }

/**
 * Our detection window; opened by Initial Frame
 */
object RemoteFaceDetectionWindow extends App {

  val logger = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  val imageDimensions = Dimensions(width = 512, height = 288)
  val detector = FaceDetector.defaultCascadeFile(imageDimensions)

  //val webcamSource = Webcam.source(deviceId = 0, dimensions = detector.dimensions)
  val webcamSource = Webcam.remote(RPiCamWebInterface(ConfigReader.host))

  val canvas = new CanvasFrame("Webcam")
  //  //Set Canvas frame to close on exit
  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

  val faceDrawer = new FaceDrawer()

  val graph = webcamSource
    .map(
      //.map(Flip.vertical)
      _.map(WithGrey.build)
        .map(detector.detect)
        .map((faceDrawer.drawFaces _).tupled)
        .map(MediaConversion.matToFrame) // convert back to a frame
        .to(ShowImageSink(canvas))
    )

  graph.map(_.run())

}
