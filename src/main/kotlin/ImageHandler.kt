import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt

fun bound(value: Int, endIndex: Int) = when {
    value < 0        -> 0
    value < endIndex -> value
    else             -> endIndex - 1
}

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
                    outputData[i, j] = (newValue / kernelDivisor).roundToInt().toInt()
                }
            }
        }
    }
    return outputData
}

fun getArrayDatasFromImage(filename: String): Array<ArrayData> {
    val inputImage = ImageIO.read(File(filename))
    //val outputfile = File("doubleCheck.png")
    //ImageIO.write(inputImage, "png", outputfile)
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

//Blur the given image x number of times.
fun blur(filenameString: String, x: Int, kernel: ArrayData, kernelDivisor: Int):BufferedImage {
    val dataArrays = getArrayDatasFromImage(filenameString)
    for (i in dataArrays.indices) {
        dataArrays[i] = convolute(dataArrays[i], kernel, kernelDivisor)
    }
    writeOutputImage("newImage.png", dataArrays)

    val dataArraysNew = getArrayDatasFromImage("newImage.png")
    for (value in 0..x) {
        println("Blur Iteration " + value)
        for (i in dataArraysNew.indices) {
            dataArraysNew[i] = convolute(dataArraysNew[i], kernel, kernelDivisor)
        }
    }
    writeOutputImage("newImage.png", dataArraysNew)
    return ImageIO.read(File("newImage.png"))
}