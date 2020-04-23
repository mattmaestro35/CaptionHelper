import java.io.File
import javax.imageio.ImageIO
import javax.swing.text.html.ImageView


import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.lang.Thread.sleep

/*Class and some main code taken from
https://rosettacode.org/wiki/Bitmap/PPM_conversion_through_a_pipe#Kotlin
To enable working with a Bitmap, since this isn't an Android project and 
so I don't get BitmapFactory.
 */
class BasicBitmapStorage(width: Int, height: Int) {
    val image = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)

    fun fill(c: Color) {
        val g = image.graphics
        g.color = c
        g.fillRect(0, 0, image.width, image.height)
    }

    fun setPixel(x: Int, y: Int, c: Color) = image.setRGB(x, y, c.getRGB())

    fun getPixel(x: Int, y: Int) = Color(image.getRGB(x, y))
}

fun main(args: Array<String>) {
    // create BasicBitmapStorage object
    val width = 640
    val height = 640
    val bbs = BasicBitmapStorage(width, height)
    val theImage: BufferedImage = ImageIO.read(File("testImage.png"))
    for (w in 0..(width - 1)) {
        for (h in 0..(height - 1)) {
            val theColor = theImage.getRGB(w, h)
            println(theColor)
        }
    }
    //get pixel colors from theImage


    //set pixel colors in bbs


    /*
    for (y in 0 until height) {
        for (x in 0 until width) {
            val c = Color(x % 256, y % 256, (x * y) % 256)
            bbs.setPixel(x, y, c)
        }
    }

    // now write the object in PPM format to ImageMagick's STDIN via a pipe
    // so it can be converted to a .jpg file and written to disk
    val pb = ProcessBuilder("convert", "-", "output_piped.jpg")
    pb.directory(null)
    pb.redirectInput(ProcessBuilder.Redirect.PIPE)
    val buffer = ByteArray(width * 3) // write one line at a time
    val proc = pb.start()
    val pStdIn = proc.outputStream
    pStdIn.use {
        val header = "P6\n$width $height\n255\n".toByteArray()
        with (it) {
            write(header)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val c = bbs.getPixel(x, y)
                    buffer[x * 3] = c.red.toByte()
                    buffer[x * 3 + 1] = c.green.toByte()
                    buffer[x * 3 + 2] = c.blue.toByte()
                }
                write(buffer)
            }
        }
    }
    sleep(1000)
    */


}
