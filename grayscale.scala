import java.awt.image.{BufferedImage, WritableRaster}
import javax.imageio.ImageIO
import java.io.File

object grayscale extends App {
	val (imgtype, imagefile, copyfile) = (args(0), args(1), args(2))
	val img = ImageIO.read(new File(imagefile))
	val mytype = img.getType
	val raster:WritableRaster = img.getRaster
	val (w,h) = (img.getWidth, img.getHeight)
	(0 until w).foreach { x=>
		(0 until h).foreach { y=>
			val arr = Array.fill[Double](3)(0.0)
			val bgr = raster.getPixel(x,y, arr)
			val avg = (bgr(0) + bgr(1) + bgr(2))/3.0
			raster.setPixel(x,y,Array(avg,avg,avg))
		}
	}
	val copy = new BufferedImage(w,h,mytype)
	copy.setData(raster)
	ImageIO.write(copy, imgtype, new File(copyfile))
}