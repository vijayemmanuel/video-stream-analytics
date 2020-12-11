package modify

import processing.Classified
import transform.WithGrey
import org.bytedeco.javacpp.helper.opencv_core.AbstractCvScalar
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
/**
 * Created by Vijay on 20/11/20.
 */

class AnnotateDrawer(fontScale: Float = 0.6f) {

  private val RedColour = new Scalar(AbstractCvScalar.RED)

  /**
   * Clones the Mat, draws squares around the faces on it using the provided [[Face]] sequence and returns the new Mat
   */
  def annotate(withGrey: WithGrey, faces: Seq[Classified], text: String): Mat = {
    val clonedMat = withGrey.orig.clone()
    for (f <- faces) {
      annotateMe(clonedMat, f, text)
    }
    clonedMat
  }

  private def annotateMe(clonedMat: Mat, f: Classified, text: String): Unit = {
    rectangle(
      clonedMat,
      new Point(f.classRect.x, f.classRect.y),
      new Point(f.classRect.x + f.classRect.width, f.classRect.y + f.classRect.height),
      RedColour,
      1,
      CV_AA,
      0
    )

    // draw the face number
    val cvPoint = new Point(f.classRect.x, f.classRect.y - 20)
    putText(clonedMat, s"$text ${f.id}", cvPoint, FONT_HERSHEY_SIMPLEX, fontScale, RedColour)
  }

}

