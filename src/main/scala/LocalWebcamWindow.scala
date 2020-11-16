import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.Dimensions
import org.bytedeco.javacv.CanvasFrame
import transform.{ Flip, MediaConversion }
import video.ImageProcessingSinks.ShowImageSink
import video.Webcam

object LocalWebcamWindow extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val canvas = new CanvasFrame("Webcam")
  //  //Set Canvas frame to close on exit
  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

  val imageDimensions = Dimensions(width = 640, height = 480)
  val localCameraSource = Webcam.local(deviceId = 0, dimensions = imageDimensions)

  val graph = localCameraSource
    .map(MediaConversion.frameToMat) // most OpenCV manipulations require a Matrix
    .map(Flip.horizontal)
    .map(MediaConversion.matToFrame) // convert back to a frame
    .to(ShowImageSink(canvas))

  graph.run()

}
