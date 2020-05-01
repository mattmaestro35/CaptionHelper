/* Image convolution code modified from https://rosettacode.org/wiki/Image_convolution */
class ArrayData(val width: Int, val height: Int) {
    var dataArray = IntArray(width * height)

    operator fun get(x: Int, y: Int) = dataArray[y * width + x]

    operator fun set(x: Int, y: Int, value: Int) {
        dataArray[y * width + x] = value
    }
}