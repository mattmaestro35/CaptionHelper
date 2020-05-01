
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.roundToInt


//Bound an integer.
fun bound(value: Int, endIndex: Int) = when {
    value < 0        -> 0
    value < endIndex -> value
    else             -> endIndex - 1
}
//Gaussion blur/convolution code based on https://rosettacode.org/wiki/Image_convolution#Kotlin
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
        throw IllegalArgumentException("Kernel must have odd width") as Throwable
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
                    outputData[i, j] = (newValue / kernelDivisor).roundToInt().toInt()
                }
            }
        }
    }
    return outputData
}

//Takes in an image, returns the RGB data of that image in an ArrayData object.
fun getArrayDatasFromImage(filename: String): Array<ArrayData> {

    val inputImage = ImageIO.read(File(filename))
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

//Write the data from an ArrayData to an image file given by filename.
fun writeOutputImage(filename: String, redGreenBlue: Array<ArrayData>) {

    val (reds, greens, blues) = redGreenBlue
    val outputImage = BufferedImage(
        reds.width, reds.height, BufferedImage.TYPE_INT_ARGB
    )

    for (y in 0 until reds.height) {
        for (x in 0 until reds.width) {

            val red = bound(reds[x , y], 256)
            val green = bound(greens[x , y], 256)
            val blue = bound(blues[x, y], 256)
            outputImage.setRGB(
                x, y, (red shl 16) or (green shl 8) or blue or -0x01000000
            )
        }
    }

    ImageIO.write(outputImage, "PNG", File(filename))
}

//Return the data of the blurred image.
fun blur(filenameString: String, kernel: ArrayData, kernelDivisor: Int): Array<ArrayData> {

    //Initial blurring.
    val dataArrays = getArrayDatasFromImage(filenameString)
    val blurAmnt = 6

    for (value in 0..blurAmnt) {
        //println("Blur Iteration " + value)
        for (i in dataArrays.indices) {
            dataArrays[i] = convolute(dataArrays[i], kernel, kernelDivisor)
        }
    }

    return dataArrays
}

fun writeText(userCaption: String, addedHeight: Int, finalImage: BufferedImage) {
    //Write text onto bottom portion
    val g: Graphics = finalImage.graphics
    val tryFontSize = (finalImage.width - 100) / (.5 * userCaption.length)

    val fontSize: Double = when {
        tryFontSize >= (addedHeight/2) -> (addedHeight/2).toDouble()
        tryFontSize <= 8 -> 8.0
        else -> tryFontSize
    }

    g.font = g.font.deriveFont(fontSize.toFloat())
    println(fontSize.toFloat())
    g.drawString(userCaption, (finalImage.width / 10), (finalImage.height - (addedHeight - fontSize.toInt()) / 2))
    g.dispose()
}


fun createFinalImage(userCaption: String, inputImageFilename: String,
                     finalImageFilename : String) {

    val theImage = ImageIO.read(File(inputImageFilename))
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
    val blurredData = blur(inputImageFilename, kernel, kernelDivisor)

    //Get just the blurred bottom.



    println("Constructing final image...")
    //Copy the original image into final image.
    for (w in 0..(width - 1)) {
        for (h in 0..(height - 1)) {
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


    writeText(userCaption, addedHeight, finalImage)



    //Save file externally.
    val outputFile = File(finalImageFilename)
    ImageIO.write(finalImage, "png", outputFile)
    println("Complete.")


}