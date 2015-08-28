# colorquantization
color quantization via kmeans

<pre>
Get commons-math here: http://commons.apache.org/proper/commons-math/download_math.cgi
To compile: scalac -cp commons-math.jar *.scala
To run:
scala -cp commons-math.jar:. grayscale jpg test.png grayscale.jpg
scala -cp commons-math.jar:. kmeansimg png test.png test4.png 4
scala -cp commons-math.jar:. kmeansimg png test.png test8.png 8

Replace minBy with maxBy in code ( use color farthest from cluster centroid, rather than nearest to cluster centroid)
scala -cp commons-math.jar:. kmeansimg png test.png test16.png 16

Look at resulting images in this repo.


