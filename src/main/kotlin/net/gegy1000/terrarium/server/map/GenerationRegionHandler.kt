package net.gegy1000.terrarium.server.map

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.gegy1000.terrarium.server.capability.TerrariumWorldData
import net.gegy1000.terrarium.server.map.adapter.CoastlineAdapter
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.generator.EarthGenerationHandler
import net.minecraft.util.math.MathHelper
import java.util.concurrent.TimeUnit

class GenerationRegionHandler(val worldData: TerrariumWorldData, val generator: EarthGenerationHandler) {
    companion object {
        val adapters = arrayOf(CoastlineAdapter)
    }

    private val cache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(9)
            .build(object : CacheLoader<RegionTilePos, GenerationRegion>() {
                override fun load(pos: RegionTilePos) = generate(pos)
            })

    private val scaledDataSize = MathHelper.floor(GenerationRegion.SAMPLE_SIZE * this.generator.finalScale)

    private val regionSampleSize = GenerationRegion.SAMPLE_SIZE + 1

    private val sampledHeights = ShortArray(this.regionSampleSize * this.regionSampleSize)
    private val sampledGlobs = Array(this.regionSampleSize * this.regionSampleSize, { GlobType.NO_DATA })

    operator fun get(globalX: Int, globalZ: Int): GenerationRegion {
        val pos = RegionTilePos(MathHelper.floor(globalX.toDouble() / GenerationRegion.SIZE), MathHelper.floor(globalZ.toDouble() / GenerationRegion.SIZE))
        return this[pos]
    }

    operator fun get(position: RegionTilePos) = this.cache[position]!!

    private fun generate(position: RegionTilePos): GenerationRegion {
        this.generator.initializeSeed(position)

        val minCoordinate = position.getMinCoordinate(generator.settings)
        val maxCoordinate = position.getMaxCoordinate(generator.settings).addGlobal(2.0, 2.0)
        val overpassTile = worldData.overpassSource.sampleArea(minCoordinate, maxCoordinate)

        val resultHeights = this.generateHeights(minCoordinate, maxCoordinate)
        val resultGlobs = this.generateGlobcover(overpassTile, minCoordinate, maxCoordinate)

        return GenerationRegion(position, minCoordinate, scaledDataSize, resultHeights, resultGlobs)
    }

    private fun generateHeights(minCoordinate: Coordinate, maxCoordinate: Coordinate): ShortArray {
        worldData.heightSource.sampleArea(sampledHeights, minCoordinate, maxCoordinate)

        val resultHeights = ShortArray(scaledDataSize * scaledDataSize)
        generator.scaleHeightRegion(resultHeights, sampledHeights, GenerationRegion.SAMPLE_SIZE + 1, GenerationRegion.SAMPLE_SIZE + 1, scaledDataSize, scaledDataSize)

        return resultHeights
    }

    private fun generateGlobcover(overpassTile: OverpassTileAccess, minCoordinate: Coordinate, maxCoordinate: Coordinate): Array<GlobType> {
        worldData.globSource.sampleArea(sampledGlobs, minCoordinate, maxCoordinate)

        val resultGlobs = Array(scaledDataSize * scaledDataSize, { GlobType.NO_DATA })
        generator.scaleGlobRegion(resultGlobs, sampledGlobs, GenerationRegion.SAMPLE_SIZE + 1, GenerationRegion.SAMPLE_SIZE + 1, scaledDataSize, scaledDataSize)

        val originX = MathHelper.floor(minCoordinate.blockX)
        val originZ = MathHelper.floor(minCoordinate.blockZ)
        adapters.forEach { adapter ->
            adapter.adaptGlobcover(generator.settings, overpassTile, resultGlobs, originX, originZ, scaledDataSize, scaledDataSize)
        }

        return resultGlobs
    }
}
