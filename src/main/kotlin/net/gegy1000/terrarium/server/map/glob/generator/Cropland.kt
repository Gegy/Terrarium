package net.gegy1000.terrarium.server.map.glob.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.minecraft.block.BlockCrops
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockFarmland
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom
import net.minecraft.world.gen.layer.IntCache
import java.util.Random

open class Cropland(type: GlobType) : GlobGenerator(type) {
    companion object {
        const val LAYER_WHEAT = 0
        const val LAYER_CARROTS = 1
        const val LAYER_POTATOES = 2
        const val CROP_COUNT = 3

        const val LAYER_FENCE = 3

        val WATER = Blocks.WATER.defaultState
        val FARMLAND = Blocks.FARMLAND.defaultState.withProperty(BlockFarmland.MOISTURE, 7)
        val COARSE_DIRT = Blocks.DIRT.defaultState.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT)

        val WHEAT = Blocks.WHEAT.defaultState
        val CARROTS = Blocks.CARROTS.defaultState
        val POTATOES = Blocks.POTATOES.defaultState

        val FENCE = Blocks.OAK_FENCE.defaultState
    }

    lateinit var cropSelector: GenLayer

    override fun createLayers(world: World) {
        var layer: GenLayer = SelectCropLayer(1)
        layer = GenLayerVoronoiZoom(1000, layer)
        layer = GenLayerFuzzyZoom(2000, layer)
        layer = GenLayerVoronoiZoom(3000, layer)
        layer = AddCropFencesLayer(4000, layer)
        layer = ConnectCropFencesLayer(5000, layer)

        this.cropSelector = layer
        this.cropSelector.initWorldGenSeed(world.seed)
    }

    override fun coverDecorate(globBuffer: Array<GlobType>, heightBuffer: IntArray, primer: ChunkPrimer, random: Random, x: Int, z: Int) {
        val cropLayer = this.sampleChunk(this.cropSelector, x, z)

        this.foreach(globBuffer) { localX: Int, localZ: Int ->
            val age = random.nextInt(8)
            val bufferIndex = localX + localZ * 16

            val cover = cropLayer[bufferIndex]

            if (cover == Cropland.LAYER_FENCE) {
                val y = heightBuffer[bufferIndex]
                primer.setBlockState(localX, y, localZ, Cropland.COARSE_DIRT)
                primer.setBlockState(localX, y + 1, localZ, Cropland.FENCE)
            } else {
                val state = when (cover) {
                    Cropland.LAYER_WHEAT -> Cropland.WHEAT.withProperty(BlockCrops.AGE, age)
                    Cropland.LAYER_CARROTS -> Cropland.CARROTS.withProperty(BlockCrops.AGE, age)
                    Cropland.LAYER_POTATOES -> Cropland.POTATOES.withProperty(BlockCrops.AGE, age)
                    else -> Cropland.FENCE
                }

                if (random.nextInt(20) != 0) {
                    val y = heightBuffer[bufferIndex]
                    if (primer.getBlockState(localX, y, localZ).block is BlockFarmland) {
                        primer.setBlockState(localX, y + 1, localZ, state)
                    }
                }
            }
        }
    }

    override fun getCover(x: Int, z: Int, random: Random) = Cropland.FARMLAND
}

class SelectCropLayer(seed: Long) : GenLayer(seed) {
    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val result = IntCache.getIntCache(areaWidth * areaHeight)
        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                this.initChunkSeed((areaX + x).toLong(), (areaY + z).toLong())
                result[x + z * areaWidth] = this.nextInt(Cropland.CROP_COUNT)
            }
        }
        return result
    }
}

class AddCropFencesLayer(seed: Long, parent: GenLayer) : GenLayer(seed) {
    init {
        this.parent = parent
    }

    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val sampleX = areaX - 1
        val sampleZ = areaY - 1
        val sampleWidth = areaWidth + 2
        val sampleHeight = areaHeight + 2
        val parent = this.parent.getInts(sampleX, sampleZ, sampleWidth, sampleHeight)

        val result = IntCache.getIntCache(areaWidth * areaHeight)

        var last: Int
        for (z in 0..areaHeight - 1) {
            last = -1
            for (x in 0..areaWidth - 1) {
                val sample = parent[x + 1 + (z + 1) * sampleWidth]
                if (last != sample && last != -1) {
                    result[x + z * areaWidth] = Cropland.LAYER_FENCE
                } else {
                    result[x + z * areaWidth] = sample
                }
                last = sample
            }
        }

        for (x in 0..areaWidth - 1) {
            last = -1
            for (z in 0..areaWidth - 1) {
                val sample = parent[x + 1 + (z + 1) * sampleWidth]
                val resultSample = result[x + z * areaWidth]
                if (resultSample != Cropland.LAYER_FENCE) {
                    if (last != sample && last != -1) {
                        result[x + z * areaWidth] = Cropland.LAYER_FENCE
                    } else {
                        result[x + z * areaWidth] = sample
                    }
                }
                last = sample
            }
        }

        return result
    }
}

class ConnectCropFencesLayer(seed: Long, parent: GenLayer) : GenLayer(seed) {
    init {
        this.parent = parent
    }

    override fun getInts(areaX: Int, areaY: Int, areaWidth: Int, areaHeight: Int): IntArray {
        val sampleX = areaX - 1
        val sampleZ = areaY - 1
        val sampleWidth = areaWidth + 2
        val sampleHeight = areaHeight + 2
        val parent = this.parent.getInts(sampleX, sampleZ, sampleWidth, sampleHeight)

        val result = IntCache.getIntCache(areaWidth * areaHeight)

        for (z in 0..areaHeight - 1) {
            for (x in 0..areaWidth - 1) {
                val parentX = x + 1
                val parentZ = z + 1
                val sample = parent[parentX + parentZ * sampleWidth]
                var type = sample
                if (sample != Cropland.LAYER_FENCE) {
                    val east = parent[(parentX + 1) + parentZ * sampleWidth] == Cropland.LAYER_FENCE
                    val west = parent[(parentX - 1) + parentZ * sampleWidth] == Cropland.LAYER_FENCE
                    val south = parent[parentX + (parentZ + 1) * sampleWidth] == Cropland.LAYER_FENCE
                    if (east && south) {
                        if (parent[(parentX + 1) + (parentZ + 1) * sampleWidth] != Cropland.LAYER_FENCE) {
                            type = Cropland.LAYER_FENCE
                        }
                    } else if (west && south) {
                        if (parent[(parentX - 1) + (parentZ + 1) * sampleWidth] != Cropland.LAYER_FENCE) {
                            type = Cropland.LAYER_FENCE
                        }
                    }
                }
                result[x + z * areaWidth] = type
            }
        }

        return result
    }
}
