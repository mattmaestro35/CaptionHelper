import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {

    //User prompts for input
    println("Please enter the filename of the PNG you wish to add a caption to.")
    val inputImageFilename = readLine() + ".png"
    println("Please enter the desired output filename.")
    val finalImageFilename = readLine() + ".png"
    println("Please enter your caption.")
    val userCaption = readLine()



    if (inputImageFilename != null && finalImageFilename != null && userCaption != null) {




        val classLoader = Thread.currentThread().contextClassLoader
        val aPic = classLoader.getResource(inputImageFilename);

        when (ImageIO.read(aPic)) {
            null -> println ("Error when retrieving image.")
            else -> createFinalImage(userCaption, inputImageFilename, finalImageFilename)
        }
    } else {
        println ("Invalid input.")
    }

}