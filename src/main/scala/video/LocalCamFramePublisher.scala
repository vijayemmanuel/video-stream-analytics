package video

import akka.actor.{ ActorLogging, DeadLetterSuppression, Props }
import akka.stream.{ Attributes, Outlet, SourceShape }
//import akka.stream.actor.ActorPublisher
//import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import akka.stream.stage.{ GraphStage, GraphStageLogic, OutHandler }
import video.LocalCamFramePublisher.{ buildGrabber }
import org.bytedeco.javacv.FrameGrabber.ImageMode
import org.bytedeco.javacv.{ FFmpegFrameGrabber, FFmpegLogCallback, Frame, FrameGrabber, OpenCVFrameGrabber, VideoInputFrameGrabber }

/**
 * Actor that backs the Akka Stream source
 */
private[video] class LocalCamFramePublisher(
    devicePath: String,
    imageWidth: Int,
    imageHeight: Int,
    bitsPerPixel: Int,
    imageMode: ImageMode
) extends GraphStage[SourceShape[Frame]] {

  /*private implicit val ec = context.dispatcher

  // Lazy so that nothing happens until the flow begins
  private lazy val grabber: FrameGrabber = buildGrabber(
    devicePath = devicePath,
    imageWidth = imageWidth,
    imageHeight = imageHeight,
    bitsPerPixel = bitsPerPixel,
    imageMode = imageMode
  )

  def receive: Receive = {
    case _: Request => emitFrames()
    case Continue => emitFrames()
    case Cancel => onCompleteThenStop()
    case unexpectedMsg => log.warning(s"Unexpected message: $unexpectedMsg")
  }

  private def emitFrames(): Unit = {
    if (isActive && totalDemand > 0) {
      /*
        Grabbing a frame is a blocking I/O operation, so we don't send too many at once.
       */
      grabFrame().foreach(onNext)
      if (totalDemand > 0) {
        Thread.sleep(100)
        self ! Continue
      }
    }
  }

  private def grabFrame(): Option[Frame] = {
    Option(grabber.grab())
  }
 */
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    //private var buffer = ByteString.empty

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        emitFrames()
      }
    })

    // Lazy so that nothing happens until the flow begins
    private lazy val grabber: FrameGrabber = buildGrabber(
      devicePath = devicePath,
      imageWidth = imageWidth,
      imageHeight = imageHeight,
      bitsPerPixel = bitsPerPixel,
      imageMode = imageMode
    )

    private def emitFrames(): Unit = {
      grabFrame() match {
        case Some(f) => push(out, f)
        case None => completeStage()
      }
    }

    private def grabFrame(): Option[Frame] = {
      try {
        val frame = grabber.grab()
        Some(frame)
      } catch {
        case _: Exception => None
      }
    }

  }
  val out = Outlet[Frame]("Frame.out")
  override def shape: SourceShape[Frame] = SourceShape.of(out)
}

object LocalCamFramePublisher {

  /*def props(devicePath: String, width: Int, height: Int, bitsPerPixel: Int, imageMode: ImageMode): Props =
    Props(
      new LocalCamFramePublisher(
        devicePath = devicePath,
        imageWidth = width,
        imageHeight = height,
        bitsPerPixel = bitsPerPixel,
        imageMode = imageMode
      )
    )

  private case object Continue extends DeadLetterSuppression*/

  // Building a started grabber seems finicky if not synchronised; there may be some freaky stuff happening somewhere.
  private[video] def buildGrabber(
    devicePath: String,
    imageWidth: Int,
    imageHeight: Int,
    bitsPerPixel: Int,
    imageMode: ImageMode
  ): FrameGrabber = synchronized {
    val g = FrameGrabber.createDefault(devicePath)
    g.setImageWidth(imageWidth)
    g.setImageHeight(imageHeight)
    g.setBitsPerPixel(bitsPerPixel)
    g.setImageMode(imageMode)
    g.start()
    g
  }
}

