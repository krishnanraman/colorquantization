import java.awt.image.{BufferedImage, WritableRaster}
import javax.imageio.ImageIO
import java.io.File
import scala.collection.JavaConversions._
import org.apache.commons.math3.ml.clustering.{KMeansPlusPlusClusterer, DoublePoint}
import org.apache.commons.math3.ml.distance.EuclideanDistance
import org.apache.commons.math3.stat.descriptive.moment.{Mean, Variance, Skewness}

/* 
compute a unique signature ( aka hashcode ) per image (png/jpg/gif)
If image A => sig A
  image B = brighter version of image A
  image C = darker version of A
  image D = version of A with different resolution
We'd like sig B, C, D to be "close" to sig A
Close ? Want SMALL Euclidean distance between hashcodes

This hashcode is a simple vector in R-36.

To run: 
scala -cp commonsmath.jar:. png foo.png foocopy.png
This will read foo.png, compute & print its R-36 signature
and create a foocopy.png with an 8 color palette.

*/
object cbir extends App {

	// first 3 moments
	def stats(x:Array[Double]) = {
		List(new Mean().evaluate(x, 0, x.size),
		new Variance().evaluate(x, 0, x.size),
		new Skewness().evaluate(x, 0, x.size))
	}

	val (imgtype, imagefile, copyfile) = (args(0), args(1), args(2))

	// read an image & get its raster
	val img = ImageIO.read(new File(imagefile))
	val raster:WritableRaster = img.getRaster
	val (w,h) = (img.getWidth, img.getHeight)

	// extract all colors from raster
	val allColors = (0 until w).map { x=>
		(0 until h).map { y=>
			val arr = Array.fill[Double](3)(0.0)
			raster.getPixel(x,y, arr)
			new DoublePoint(arr)
		}
	}.flatten

	// find 8 dominant colors of image via kmeans
	val k = 8
	val kmeans = new KMeansPlusPlusClusterer[DoublePoint](k, 1000)
	kmeans.getRandomGenerator().setSeed(1234567L)
	val centroids = kmeans.cluster(allColors.toIterable)
	val colors = centroids.map{ x=> x.getCenter.getPoint }
	val euclidean = new EuclideanDistance()

	// replace all colors with colors in the palette below
	// this is just a random 8-color palette
	val eightColorPalette = Array(
		Array(255.0,0,0),
		Array(0,255.0,0),
		Array(0,0,255.0),
		Array(255.0,255.0,0),
		Array(255.0,0.0,255.0),
		Array(0,255.0,255.0),
		Array(0.0,0,0),
		Array(255.0,255.0,255.0))

	// update raster with 8 color palette
	(0 until w).foreach { x=>
		(0 until h).foreach { y=>
			val arr = Array.fill[Double](3)(0.0)
			raster.getPixel(x,y, arr) // loads the BGR color into arr
			val closestIdx = colors
				.zipWithIndex
				.map{ ci => 
					val (c,idx) = ci
					(idx,euclidean.compute(c,arr)) 
				}
				.minBy{ x=> x._2 } // want smallest euclidean
				._1 // index of smallest euclidean

			// instead of replacing each pixel with closest dominant color, we reach into our palette
			// val closestColor = colors(closestIdx) 
			val closestColor = eightColorPalette(closestIdx)

			raster.setPixel(x,y,closestColor) // replace pixel color in raster
		}
	}

	// extract all the colors again from the updated raster
	val data:Seq[Array[Double]] = (0 until w).map { x=>
		(0 until h).map { y=>
			val arr = Array.fill[Double](3)(0.0)
			raster.getPixel(x,y, arr)
			arr
		}
	}.flatten.toSeq

	// compute signature
	val n = data.size
	val sig = data
	.grouped(n/4)
	.map{ gp:Seq[Array[Double]] =>
		stats(gp.map{ x=> x(0)}.toArray) ++ // B
		stats(gp.map{ x=> x(1)}.toArray)  ++ // G
		stats(gp.map{ x=> x(2)}.toArray) // R
	}
	.reduceLeft(_ ++ _)
	.mkString(",")

	println(sig)
	val pw = new java.io.PrintWriter(args(1)+"_sig.txt")
	pw.println(sig)
	pw.flush
	pw.close

	// make the 8-color palette copy
	val copy = new BufferedImage(w,h,img.getType)
	copy.setData(raster)
	ImageIO.write(copy, imgtype, new File(copyfile))
}

