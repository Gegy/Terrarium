package net.gegy1000.terrarium.server.util

import net.minecraft.util.math.MathHelper
import java.awt.Point


object Interpolation {
    fun cosine(y1: Double, y2: Double, intermediate: Double): Double {
        val cos = (1.0 - Math.cos(intermediate * Math.PI)) / 2.0
        return y1 * (1.0 - cos) + y2 * cos
    }

    inline fun interpolateLine(originX: Double, originY: Double, targetX: Double, targetY: Double, thick: Boolean = false, set: (Point) -> Unit) {
        val currentPoint = Point(MathHelper.floor(originX), MathHelper.floor(originY))

        var horizontal = false

        var deltaX = Math.max(1, Math.abs(MathHelper.floor(targetX) - MathHelper.floor(originX)))
        var deltaY = Math.max(1, Math.abs(MathHelper.floor(targetY) - MathHelper.floor(originY)))

        val signumX = Integer.signum(MathHelper.floor(targetX) - MathHelper.floor(originX))
        val signumY = Integer.signum(MathHelper.floor(targetY) - MathHelper.floor(originY))

        if (deltaY > deltaX) {
            val tmp = deltaX
            deltaX = deltaY
            deltaY = tmp
            horizontal = true
        }

        var longLength = (2 * deltaY - deltaX).toDouble()

        for (i in 0..deltaX) {
            set(currentPoint)

            while (longLength >= 0) {
                if (horizontal) {
                    currentPoint.x += signumX
                } else {
                    currentPoint.y += signumY
                }

                if (thick) {
                    set(currentPoint)
                }

                longLength -= 2 * deltaX
            }

            if (horizontal) {
                currentPoint.y += signumY
            } else {
                currentPoint.x += signumX
            }

            if (thick) {
                set(currentPoint)
            }

            longLength += 2 * deltaY
        }
    }
}
