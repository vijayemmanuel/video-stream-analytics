package processing

import org.bytedeco.javacpp.opencv_core.Rect

/**
 * Created by Lloyd on 2/14/16.
 */

/**
 * Holds an id and an OpenCV Rect defining the corners of a rectangle.
 *
 * There is nothing *face* specific in this class per say; it can hold ids and Rects for any detected
 * object
 */
case class Classified(id: Long, classRect: Rect)
