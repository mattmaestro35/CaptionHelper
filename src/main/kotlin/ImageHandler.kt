
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt


//Bound an integer.
fun bound(value: Int, endIndex: Int) = when {
    value < 0        -> 0
    value < endIndex -> value
    else             -> endIndex - 1
}
//Gaussian blur/convolution code based on https://rosettacode.org/wiki/Image_convolution#Kotlin
fun convolute(
    inputData: ArrayData,
    kernel: ArrayData,
    kernelDivisor: Int
): ArrayData {
    val inputWidth = inputData.width
    val inputHeight = inputData.height
    val kernelWidth = kernel.width
    val kernelHeight = kernel.height

    if (kernelWidth <= 0 || (kernelWidth and 1) != 1)
        throw IllegalArgumentException("Kernel must have odd width")
    if (kernelHeight <= 0 || (kernelHeight and 1) != 1)
        throw IllegalArgumentException("Kernel must have odd height")

    val kernelWidthRadius = kernelWidth ushr 1
    val kernelHeightRadius = kernelHeight ushr 1

    val outputData = ArrayData(inputWidth, inputHeight)

    for (i in inputWidth - 1 downTo 0) {
        for (j in inputHeight - 1 downTo 0) {
            var newValue = 0.0
            for (kw in kernelWidth - 1 downTo 0) {
                for (kh in kernelHeight - 1 downTo 0) {
                    newValue += kernel[kw, kh] * inputData[
                            bound(i + kw - kernelWidthRadius, inputWidth),
                            bound(j + kh - kernelHeightRadius, inputHeight)
                    ].toDouble()
                    outputData[i, j] = (newValue / kernelDivisor).roundToInt()
                }
            }
        }
    }
    return outputData
}

//Takes in an image, returns the RGB data of that image in an ArrayData object.
fun getArrayDataFromImage(inputImage: BufferedImage): Array<ArrayData> {

    val width = inputImage.width
    val height = inputImage.height

    val rgbData = inputImage.getRGB(0, 0, width, height, null, 0, width)
    val reds = ArrayData(width, height)
    val greens = ArrayData(width, height)
    val blues = ArrayData(width, height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val rgbValue = rgbData[y * width + x]
            reds[x, y] = (rgbValue ushr 16) and 0xFF
            greens[x,y] = (rgbValue ushr 8) and 0xFF
            blues[x, y] = rgbValue and 0xFF
        }
    }
    return arrayOf(reds, greens, blues)
}

//Return the data of the blurred image.
fun blur(inputImage: BufferedImage, kernel: ArrayData, kernelDivisor: Int): Array<ArrayData> {

    //Initial blurring.
    val dataArrays = getArrayDataFromImage(inputImage)
    val blurAmnt = 6

    for (value in 0..blurAmnt) {
        //println("Blur Iteration " + value)
        for (i in dataArrays.indices) {
            dataArrays[i] = convolute(dataArrays[i], kernel, kernelDivisor)
        }
    }

    return dataArrays
}

//Writes text onto the bottom portion of the combined blurred/original image.
fun writeText(userCaption: String, addedHeight: Int, finalImage: BufferedImage, color: String) {
    val g: Graphics = finalImage.graphics
    val tryFontSize = (finalImage.width - 100) / (.5 * userCaption.length)

    //Constrain font size.
    val fontSize: Double = when {
        tryFontSize >= (addedHeight/2) -> (addedHeight/2).toDouble()
        tryFontSize <= 8 -> 8.0
        else -> tryFontSize
    }
    g.font = g.font.deriveFont(fontSize.toFloat())

    //Decide caption color.
    when (color) {
        "black" -> g.color = Color.BLACK
        else -> g.color = Color.WHITE
    }

    //println(fontSize.toFloat())
    g.drawString(userCaption, (finalImage.width / 10), (finalImage.height - (addedHeight - fontSize.toInt()) / 2))
    g.dispose()
}


fun createFinalImage(userCaption: String, inputImageFilename: String,
                     finalImageFilename : String, color: String) {
    //Load image from resources folder.
    val classLoader = Thread.currentThread().contextClassLoader
    val aPic = classLoader.getResource(inputImageFilename)
    val theImage = ImageIO.read(aPic)

    println("Image found successfully.")

    //Create kernel to use for blurring algorithm.
    val kernelWidth = 7
    val kernelHeight = 7
    val kernel = ArrayData(kernelWidth, kernelHeight)

    //Integers here correspond to relative weight of each pixel's RGB value when blurring.
    //More evenly spread weight = heavier blurring.
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

    //Create matrix from kernelArray
    var y = 0
    for (i in 0 until kernelHeight) {
        for (j in 0 until kernelWidth) {
            kernel[j, i] = kernelArray[y++]
        }
    }

    //Create BufferedImage that will become the final image. Height is increased by 100 pixels.
    val width = theImage.width

    val height = theImage.height
    val addedHeight = height / 6
    val finalImage = BufferedImage(width, height + addedHeight, BufferedImage.TYPE_3BYTE_BGR)

    //Create a blurred version of the image by filename.
    println("Blurring...")
    val blurredData = blur(theImage, kernel, kernelDivisor)

    println("Constructing final image...")
    //Copy the original image into final image.
    for (w in 0 until width) {
        for (h in 0 until height) {
            val rgb = theImage.getRGB(w, h)
            finalImage.setRGB(w, h, rgb)
        }
    }

    //Copy the bottom portion of blurred image into the final image.
    for (w in 0 until (width - 1)) {
        for (h in (height - addedHeight) until height) {

            //Data from blurred Image.
            val (reds, greens, blues) = blurredData

            val red = bound(reds[w , h], 256)
            val green = bound(greens[w , h], 256)
            val blue = bound(blues[w, h], 256)
            //Set color of a pixel in final pixel.
            finalImage.setRGB(
                w, h + addedHeight, (red shl 16) or (green shl 8) or blue or -0x01000000
            )

        }
    }

    writeText(userCaption, addedHeight, finalImage, color)

    //Save file externally.
    val path = "src/main/resources/$finalImageFilename"
    val outputFile = File(path)
    ImageIO.write(finalImage, "png", outputFile)
    println("Complete.")


}