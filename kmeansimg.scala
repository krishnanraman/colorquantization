import java.awt.image.{BufferedImage, WritableRaster}
import javax.imageio.ImageIO
import java.io.File
import scala.collection.JavaConversions._
import org.apache.commons.math3.ml.clustering.{KMeansPlusPlusClusterer, DoublePoint}
import org.apache.commons.math3.ml.distance.EuclideanDistance

object kmeansimg extends App {
	val (imgtype, imagefile, copyfile) = (args(0), args(1), args(2))
	val img = ImageIO.read(new File(imagefile))
	val raster:WritableRaster = img.getRaster
	val (w,h) = (img.getWidth, img.getHeight)

	val allColors = (0 until w).map { x=>
		(0 until h).map { y=>
			val arr = Array.fill[Double](3)(0.0)
			raster.getPixel(x,y, arr)
			new DoublePoint(arr)
		}
	}.flatten

	// find k dominant colors of image
	val k = args(3).toInt
	val kmeans = new KMeansPlusPlusClusterer[DoublePoint](k)
	val centroids = kmeans.cluster(allColors.toIterable)
	val colors = centroids.map{ x=> x.getCenter.getPoint }
	val euclidean = new EuclideanDistance()

	(0 until w).foreach { x=>
		(0 until h).foreach { y=>
			val arr = Array.fill[Double](3)(0.0)
			raster.getPixel(x,y, arr)
			val closestIdx = colors
				.zipWithIndex
				.map{ ci => 
					val (c,idx) = ci
					(idx,euclidean.compute(c,arr)) 
				}
				.minBy{ x=> x._2 } // want smallest euclidean
				._1 // index of smallest euclidean

			val closestColor = colors(closestIdx)
			raster.setPixel(x,y,closestColor) // replac pixel color with closest dominant color
		}
	}

	val copy = new BufferedImage(w,h,img.getType)
	copy.setData(raster)
	ImageIO.write(copy, imgtype, new File(copyfile))
}

