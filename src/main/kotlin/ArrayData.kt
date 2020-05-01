class ArrayData(val width: Int, val height: Int) {
    private var dataArray = IntArray(width * height)

    operator fun get(x: Int, y: Int) = dataArray[y * width + x]

    operator fun set(x: Int, y: Int, value: Int) {
        dataArray[y * width + x] = value
    }
}