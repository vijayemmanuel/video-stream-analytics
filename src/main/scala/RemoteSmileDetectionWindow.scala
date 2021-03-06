import RemoteFaceDetectionWindow.drawer
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.{ ConfigReader, Dimensions }
import modify.{ AnnotateDrawer }
import org.bytedeco.javacv.CanvasFrame
import org.slf4j.LoggerFactory
import processing.{ FaceDetector, SmileDetector }
import transform.{ Flip, MediaConversion, WithGrey }
import video.ImageProcessingSinks.ShowImageSink
import video.{ RPiCamWebInterface, Webcam }

/**
 * Our detection window; opened by Initial Frame
 */
object RemoteSmileDetectionWindow extends App {

  val logger = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem()
  //implicit val materializer = ActorMaterializer()

  implicit val ec = system.dispatcher

  val imageDimensions = Dimensions(width = 512, height = 288)
  val detector = FaceDetector.defaultCascadeFile(imageDimensions)
  val smileDetector = SmileDetector.smileCascadeFile(imageDimensions)

  val webcamSource = Webcam.remote(RPiCamWebInterface(ConfigReader.camHost, ConfigReader.camPort))

  val canvas = new CanvasFrame("Webcam")
  //Set Canvas frame to close on exit
  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

  val drawer = new AnnotateDrawer()

  val graph = webcamSource
    .map(
      _.map(Flip.vertical)
        .map(WithGrey.build)
        .map(detector.detect)
        .map(smileDetector.smileDetect _)
        //.map((faceDrawer.drawFaces _).tupled)
        .map(x => drawer.annotate(x._1, x._2, "smile"))
        .map(MediaConversion.matToFrame) // convert back to a frame
        .to(ShowImageSink(canvas))
    )

  graph.map(_.run())

}
