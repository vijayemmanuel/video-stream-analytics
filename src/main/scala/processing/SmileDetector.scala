package processing

import common.Dimensions
import transform.WithGrey
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier

/**
 * Created by Vijay on 20/11/20.
 */

object SmileDetector {

  /**
   * Builds a FaceDetector with the default Haar Cascade classifier in the resource directory
   */
  def smileCascadeFile(
    dimensions: Dimensions,
    scaleFactor: Double = 1.1,
    minNeighbours: Int = 5,
    detectorFlag: HaarDetectorFlag = HaarDetectorFlag.ScaleImage,
    minSize: Dimensions = Dimensions(width = 30, height = 30),
    maxSize: Option[Dimensions] = None
  ): SmileDetector = {
    val classLoader = this.getClass.getClassLoader
    val smileXml = classLoader.getResource("haarcascade_smile.xml").getPath
    new SmileDetector(
      dimensions = dimensions,
      classifierPath = smileXml,
      scaleFactor = scaleFactor,
      minNeighbours = minNeighbours,
      detectorFlag = detectorFlag,
      minSize = minSize,
      maxSize = maxSize
    )
  }
}

class SmileDetector(
    val dimensions: Dimensions,
    classifierPath: String,
    scaleFactor: Double = 1.3,
    minNeighbours: Int = 3,
    detectorFlag: HaarDetectorFlag = HaarDetectorFlag.ScaleImage,
    minSize: Dimensions = Dimensions(width = 30, height = 30),
    maxSize: Option[Dimensions] = None
) {

  private val smileCascade = new CascadeClassifier(classifierPath)

  private val minSizeOpenCV = new Size(minSize.width, minSize.height)
  private val maxSizeOpenCV = maxSize.map(d => new Size(d.width, d.height)).getOrElse(new Size())

  /**
   * Given a frame matrix, a series of detected smiles within faces
   */
  def smileDetect(identifiedFaces: (WithGrey, Seq[Classified])): (WithGrey, Seq[Classified]) = {
    val currentGreyMat = identifiedFaces._1.grey
    val smileRects = findSmiles(currentGreyMat)

    val smiles: Seq[Classified] = for {
      i <- 0 until identifiedFaces._2.length
      j <- 0L until smileRects.size()
      face = identifiedFaces._2(i)
      smile = smileRects.get(j)
      x = face.classRect.x()
      y = face.classRect.y()
      w = face.classRect.width()
      h = face.classRect.height()
      x_s = smile.x()
      y_s = smile.y()
      w_s = smile.width()
      h_s = smile.height()
      if ((x <= x_s) && (y <= y_s) && (x + w >= x_s + w_s) && (y + h >= y_s + h_s))
    } yield Classified(j, smile)
    (identifiedFaces._1, smiles)
  }

  private def findSmiles(greyMat: Mat): RectVector = {
    val smileRects = new RectVector()
    smileCascade.detectMultiScale(greyMat, smileRects)
    smileRects
  }

}
