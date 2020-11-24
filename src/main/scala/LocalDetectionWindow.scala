import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.Dimensions
import org.bytedeco.javacv.CanvasFrame
import processing.DetectMotion
import transform.{ Flip, MediaConversion }
import video.ImageProcessingSinks.ShowImageSink
import video.Webcam

object LocalDetectionWindow extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val canvas = new CanvasFrame("Webcam")
  //  //Set Canvas frame to close on exit
  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

  val imageDimensions = Dimensions(width = 640, height = 480)
  val localCameraSource = Webcam.local(
    devicePath = "/Users/vijay/Downloads/WhatsAppVideo2019-11-23at18.36.19.mp4",
    dimensions = imageDimensions
  )

  val graph = localCameraSource
    .map(MediaConversion.frameToMat)
    .map(Flip.horizontal)
    .grouped(2)
    .via(DetectMotion())
    .map(MediaConversion.matToFrame)
    .to(ShowImageSink(canvas))

  graph.run()

}
