import javax.imageio.ImageIO

fun main() {

    //User prompts for input
    println("Please enter the filename of the PNG you wish to add a caption to.")
    val inputImageFilename = readLine() + ".png"
    println("Please enter the desired output filename.")
    val finalImageFilename = readLine() + ".png"
    println("Please enter your caption.")
    val userCaption = readLine() + ""
    println("Please input text color, 'black' or 'white'.")
    val color = when (readLine()) {
        "black" -> "black"
        "white" -> "white"
        else -> {
            println("Invalid color input. Defaulting to white..")
            "white"
        }
    }

    val classLoader = Thread.currentThread().contextClassLoader
    try {
        val aPic = classLoader.getResource(inputImageFilename)

        when (ImageIO.read(aPic)) {
            null -> println ("Error when retrieving image.")
            else -> createFinalImage(userCaption, inputImageFilename, finalImageFilename, color)
        }
    } catch (e: Exception) {
        println("Error when retrieving image: $e")
    }


}