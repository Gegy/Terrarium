package net.gegy1000.terrarium.server.map.adapter

import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.util.Interpolation
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.util.math.MathHelper
import java.awt.image.BufferedImage
import java.io.File
import java.util.Stack
import javax.imageio.ImageIO

object CoastlineAdapter : RegionAdapter {
    private const val OCEAN = 0
    private const val LAND = 1
    private const val COAST = 2

    private const val COAST_UP = 4
    private const val COAST_DOWN = 8

    override fun adaptGlobcover(settings: EarthGenerationSettings, overpassTile: OverpassTileAccess, globBuffer: Array<GlobType>, x: Int, y: Int, width: Int, height: Int) {
        val coastlines = overpassTile.elements.filter { it.isType("natural", "coastline") }

        if (!coastlines.isEmpty()) {
            val landmap = IntArray(width * height) { if (globBuffer[it] == GlobType.WATER) OCEAN else LAND }
            writeStage(x, y, 0, width, height, landmap)

            val floodPoints = HashMap<FloodPoint, Int>()

            coastlines.forEach {
                val nodes = it.nodes.map { overpassTile.nodes[it] }

                var i = 0
                while (++i < nodes.size) {
                    val current = nodes[i - 1]
                    val next = nodes[i]

                    val currentCoordinate = Coordinate.fromLatLng(settings, current.latitude, current.longitude)
                    val originX = currentCoordinate.blockX
                    val originY = currentCoordinate.blockZ

                    var nextCoordinate = Coordinate.fromLatLng(settings, next.latitude, next.longitude)

                    while (Math.abs(nextCoordinate.blockX - originX) < 2.0 && Math.abs(nextCoordinate.blockZ - originY) < 1.0) {
                        if (++i >= nodes.size) {
                            break
                        }
                        val node = nodes[i]
                        nextCoordinate = Coordinate.fromLatLng(settings, node.latitude, node.longitude)
                    }

                    val targetX = nextCoordinate.blockX
                    val targetY = nextCoordinate.blockZ

                    val lineType = getCoastType(currentCoordinate, nextCoordinate)
                    val coastType = lineType and 12

                    Interpolation.interpolateLine(originX, originY, targetX, targetY) { point ->
                        val localX = point.x - x
                        val localY = point.y - y
                        if (localX >= 0 && localY >= 0 && localX < width && localY < height) {
                            landmap[localX + localY * width] = lineType
                            if (coastType == COAST_UP) {
                                if (localX > 0) {
                                    floodPoints.put(FloodPoint(localX - 1, localY), LAND)
                                }
                                if (localX < width - 1) {
                                    floodPoints.put(FloodPoint(localX + 1, localY), OCEAN)
                                }
                            } else if (coastType == COAST_DOWN) {
                                if (localX > 0) {
                                    floodPoints.put(FloodPoint(localX - 1, localY), OCEAN)
                                }
                                if (localX < width - 1) {
                                    floodPoints.put(FloodPoint(localX + 1, localY), LAND)
                                }
                            }
                        }
                    }
                }
            }

            writeStage(x, y, 1, width, height, landmap)

            floodPoints.forEach { (point, floodType) ->
                val sampled = landmap[point.x + point.y * width]
                if (canFlood(sampled, floodType)) {
                    floodFill(landmap, width, height, point, floodType)
                }
            }

            writeStage(x, y, 2, width, height, landmap)

            for (index in 0..globBuffer.lastIndex) {
                val glob = globBuffer[index]
                val landType = landmap[index] and 3
                if (landType == OCEAN) {
                    globBuffer[index] = GlobType.WATER
                } else if (glob == GlobType.WATER) {
                    globBuffer[index] = GlobType.GRASSLAND
                }
            }
        }
    }

    private fun floodFill(landmap: IntArray, width: Int, height: Int, origin: FloodPoint, floodType: Int) {
        val points = Stack<FloodPoint>()
        val visitedPoints = hashSetOf(origin)
        points.push(origin)
        while (!points.isEmpty()) {
            val currentPoint = points.pop()
            landmap[currentPoint.x + currentPoint.y * width] = floodType
            for (offsetY in -1..1) {
                for (offsetX in -1..1) {
                    if ((offsetX == 0 || offsetY == 0) && !(offsetX == 0 && offsetY == 0)) {
                        val neighbourX = currentPoint.x + offsetX
                        val neighbourY = currentPoint.y + offsetY
                        if (neighbourX >= 0 && neighbourY >= 0 && neighbourX < width && neighbourY < height) {
                            val sampled = landmap[neighbourX + neighbourY * width]
                            val floodPoint = FloodPoint(neighbourX, neighbourY)
                            if (canFlood(sampled, floodType) && !visitedPoints.contains(floodPoint)) {
                                points.push(floodPoint)
                                visitedPoints.add(floodPoint)
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: Remove, exists for debugging purposes
    private fun writeStage(x: Int, y: Int, stage: Int, width: Int, height: Int, coastlineMap: IntArray) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        repeat(height) { iy ->
            repeat(width) { ix ->
                val colour = when (coastlineMap[ix + iy * width]) {
                    OCEAN -> 0x0000FF
                    LAND -> 0x00FF00
                    COAST or COAST_UP -> 0xFF0000
                    COAST or COAST_DOWN -> 0x00FFFF
                    else -> 0xFFFFFF
                }
                image.setRGB(ix, iy, colour)
            }
        }
        ImageIO.write(image, "png", File("stages/${x}_${y}_${stage}.png"))
    }

    private fun canFlood(sampled: Int, flood: Int): Boolean {
        val landType = sampled and 3
        return (landType == LAND || landType == OCEAN) && landType != (flood and 3)
    }

    private fun getCoastType(currentCoordinate: Coordinate, nextCoordinate: Coordinate): Int {
        if (MathHelper.floor(nextCoordinate.blockZ) > MathHelper.floor(currentCoordinate.blockZ)) {
            return COAST or COAST_DOWN
        } else if (MathHelper.floor(nextCoordinate.blockZ) < MathHelper.floor(currentCoordinate.blockZ)) {
            return COAST or COAST_UP
        }
        return COAST
    }

    private data class FloodPoint(val x: Int, val y: Int) {
        override fun equals(other: Any?) = other is FloodPoint && other.x == x && other.y == y

        override fun hashCode() = x + y * 12000
    }
}
