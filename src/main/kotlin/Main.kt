import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {

    //User inputs
    //TODO: Work from command line
    val inputImageFilename = "noTextImage.png"
    val finalImageFilename = "YesTextImage.png"
    val userCaption = "No cost too great."


    //Try to get image to blur from file.
    val theImage : BufferedImage? = try { ImageIO.read(File(inputImageFilename))
    } catch (e: Exception) { null }

    when (theImage) {
        null -> println ("Error when retrieving image.")
        else -> createFinalImage(theImage, userCaption, inputImageFilename, finalImageFilename)
    }

}