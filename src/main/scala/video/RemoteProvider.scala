package video

sealed trait RemoteProvider {

  def width: Int

  def height: Int

  def uri: String
}

case class RPiCamWebInterface(host: String) extends RemoteProvider {

  val width = 512

  val height = 288

  //The pDelay is the interval between frames in microseconds
  override def uri: String = s"http://$host/html/cam_pic_new.php?pDelay=1000000"

}
