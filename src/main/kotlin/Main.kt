import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt


fun main(args: Array<String>) {

    /* Image convolution code modified from https://rosettacode.org/wiki/Image_convolution */
    class ArrayData(val width: Int, val height: Int) {
        var dataArray = IntArray(width * height)

        operator fun get(x: Int, y: Int) = dataArray[y * width + x]

        operator fun set(x: Int, y: Int, value: Int) {
            dataArray[y * width + x] = value
        }
    }

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


    /* Actua start of main code */
    //TODO: Separate convolution code into a separate class
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
    println("Kernel size: $kernelWidth x $kernelHeight, divisor = $kernelDivisor")

    var y = 0
    for (i in 0 until kernelHeight) {
        print("[")
        for (j in 0 until kernelWidth) {
            kernel[j, i] = kernelArray[y++]
            print(" ${kernel[j, i]} ")
        }
        println("]")
    }

    fun blur(filenameString: String, x: Int): BufferedImage {
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

    val theImage: BufferedImage = ImageIO.read(File("testImage.png"))

    val BLUR_AMNT = 10
    val blurredImage = blur("testImage.png", BLUR_AMNT)
    //TODO: Add nice error handling, try/catch
    val width = theImage.width
    val height = theImage.height
    val finalImage: BufferedImage = BufferedImage(width, height + 100, BufferedImage.TYPE_3BYTE_BGR)

    for (w in 0..(width - 1)) {
        for (h in 0..(height - 1)) {
            val rgb = theImage.getRGB(w, h)
            finalImage.setRGB(w, h, rgb)
        }
    }

    for (w in 0 .. (width - 1)) {
        for (h in (height - 100)..height - 1) {
           val blurredRGB = blurredImage.getRGB(w, h)
            finalImage.setRGB(w, h + 100, blurredRGB)
        }
    }

    val outputFile = File("finalImage.png")
    ImageIO.write(finalImage, "png", outputFile)

}