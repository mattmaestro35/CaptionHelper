import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt


fun main(args: Array<String>) {
    //Create kernel to use for blurring algorithm.
    val kernelWidth = 7
    val kernelHeight = 7
    val kernel = ArrayData(kernelWidth, kernelHeight)
    val kernelArray = arrayOf(
        2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2,
        2, 2, 3, 4, 3, 2, 2,
        2, 2, 4, 4, 4, 2, 2,
        2, 2, 3, 4, 3, 2, 2,
        2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2
    )
    val kernelDivisor = kernelArray.sum()
    //println("Kernel size: $kernelWidth x $kernelHeight, divisor = $kernelDivisor")

    var y = 0
    //Create matrix from kernelArray
    for (i in 0 until kernelHeight) {
        //print("[")
        for (j in 0 until kernelWidth) {
            kernel[j, i] = kernelArray[y++]
            //print(" ${kernel[j, i]} ")
        }
        //println("]")
    }


    //Get image to blur from file.
    val theImage: BufferedImage = ImageIO.read(File("testImage.png"))
    //Create a blurred version of the image.
    val BLUR_AMNT = 2
    val blurredImage = blur("testImage.png", BLUR_AMNT, kernel, kernelDivisor)

    /*
    val g = blurredImage.graphics;
    g.font = g.font.deriveFont(30f);
    val captionString = "No cost too great."
    g.drawString(captionString, 100, 100);
    g.dispose();
    */
    val blurredFile = File("blurredFile.png")
    //ImageIO.write(blurredImage, "png", blurredFile)

    //TODO: Add nice error handling, try/catch
    val width = theImage.width
    val height = theImage.height
    //Create BufferedImage that will become the final image. Height is increased by 100 pixels.
    val finalImage = BufferedImage(width, height + 100, BufferedImage.TYPE_3BYTE_BGR)

    //Copy the original image into final image.
    for (w in 0..(width - 1)) {
        for (h in 0..(height - 1)) {
            val rgb = theImage.getRGB(w, h)
            finalImage.setRGB(w, h, rgb)
        }
    }
    //Copy the bottom portion of blurred image into the final image.
    for (w in 0 .. (width - 1)) {
        for (h in (height - 100)..height - 1) {
           val blurredRGB = blurredImage.getRGB(w, h)
            finalImage.setRGB(w, h + 100, blurredRGB)
        }
    }

    val outputFile = File("finalImage.png")
    //Save file externally.
    ImageIO.write(finalImage, "png", outputFile)

}