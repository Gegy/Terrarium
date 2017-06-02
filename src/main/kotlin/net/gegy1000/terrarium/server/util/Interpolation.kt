package net.gegy1000.terrarium.server.util

object Interpolation {
    val buffer: ThreadLocal<DoubleArray> = ThreadLocal.withInitial({ DoubleArray(4) })

    fun bicubic(buffer: Array<DoubleArray>, scaleX: Double, scaleY: Double): Double {
        val cubic = this.buffer.get()
        cubic[0] = this.cubic(buffer[0], scaleY)
        cubic[1] = this.cubic(buffer[1], scaleY)
        cubic[2] = this.cubic(buffer[2], scaleY)
        cubic[3] = this.cubic(buffer[3], scaleY)
        return this.cubic(cubic, scaleX)
    }

    fun cubic(buffer: DoubleArray, scale: Double): Double {
        return buffer[1] + 0.5 * scale * (buffer[2] - buffer[0] + scale * (2.0 * buffer[0] - 5.0 * buffer[1] + 4.0 * buffer[2] - buffer[3] + scale * (3.0 * (buffer[1] - buffer[2]) + buffer[3] - buffer[0])))
    }
}
