package net.gegy1000.terrarium.server.world.generator

import net.gegy1000.terrarium.server.map.glob.GlobGenerator
import net.gegy1000.terrarium.server.map.glob.GlobType
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.block.BlockFalling
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldEntitySpawner
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.gen.NoiseGeneratorPerlin
import net.minecraft.world.gen.layer.IntCache
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.terraingen.PopulateChunkEvent
import net.minecraftforge.event.terraingen.TerrainGen
import java.util.EnumMap
import java.util.Random
import kotlin.reflect.full.createInstance

class EarthChunkGenerator(val world: World, seed: Long, settingsString: String) : IChunkGenerator {
    companion object {
        private val STONE = Blocks.STONE.defaultState
        private val AIR = Blocks.AIR.defaultState
        private val BEDROCK = Blocks.BEDROCK.defaultState
        private val GRAVEL = Blocks.GRAVEL.defaultState
        private val RED_SANDSTONE = Blocks.RED_SANDSTONE.defaultState
        private val SANDSTONE = Blocks.SANDSTONE.defaultState
        private val ICE = Blocks.ICE.defaultState
        private val WATER = Blocks.WATER.defaultState
    }

    val random = Random(seed)
    val settings = EarthGenerationSettings.deserialize(settingsString)
    val handler = EarthGenerationHandler(this.world, this.settings)
    val coverNoise = NoiseGeneratorPerlin(this.random, 4)
    val pos = BlockPos.MutableBlockPos()

    val globGenerators = EnumMap<GlobType, GlobGenerator>(GlobType::class.java)

    var depthBuffer = DoubleArray(256)
    val heightBuffer = IntArray(256)
    val globBuffer = Array(256, { GlobType.NO_DATA })
    val coverBuffer = Array(256, { STONE })
    val fillerBuffer = Array(256, { STONE })

    var biomeBuffer: Array<Biome>? = null

    init {
        GlobType.values().forEach {
            val generator = it.generator.createInstance()
            generator.initialize(this.world, this.globBuffer, this.heightBuffer, this.coverBuffer, this.fillerBuffer)
            this.globGenerators.put(it, generator)
        }
    }

    override fun generateChunk(chunkX: Int, chunkZ: Int): Chunk {
        this.random.setSeed(chunkX.toLong() * 341873128712L + chunkZ.toLong() * 132897987541L)

        val primer = ChunkPrimer()
        this.populateBlocks(primer, chunkX, chunkZ)
        this.biomeBuffer = this.world.biomeProvider.getBiomes(this.biomeBuffer, chunkX shl 4, chunkZ shl 4, 16, 16)
        this.generateBiome(primer, chunkX, chunkZ)

        val chunk = Chunk(this.world, primer, chunkX, chunkZ)

        val biomeArray = chunk.biomeArray
        for (biomeIndex in biomeArray.indices) {
            biomeArray[biomeIndex] = Biome.getIdForBiome(this.biomeBuffer?.get(biomeIndex)).toByte()
        }

        chunk.generateSkylightMap()

        return chunk
    }

    private fun populateBlocks(primer: ChunkPrimer, chunkX: Int, chunkZ: Int) {
        val oceanHeight = this.handler.oceanHeight
        this.handler.getHeightRegion(this.heightBuffer, chunkX, chunkZ)

        for (z in 0..15) {
            for (x in 0..15) {
                val height = this.heightBuffer[x + z * 16]
                for (y in 1..height) {
                    primer.setBlockState(x, y, z, STONE)
                }
                if (height < oceanHeight) {
                    for (y in height + 1..oceanHeight) {
                        primer.setBlockState(x, y, z, WATER)
                    }
                }
            }
        }

        if (this.settings.mapFeatures) {

        }
    }

    private fun generateBiome(primer: ChunkPrimer, chunkX: Int, chunkZ: Int) {
        val scale = 0.03125

        val globalX = chunkX shl 4
        val globalZ = chunkZ shl 4

        this.depthBuffer = this.coverNoise.getRegion(this.depthBuffer, globalX.toDouble(), globalZ.toDouble(), 16, 16, scale * 2.0, scale * 2.0, 1.0)
        this.handler.getGlobRegion(this.globBuffer, chunkX, chunkZ)

        val generators = this.globBuffer.toHashSet()
        generators.forEach {
            val generator = this.globGenerators[it]
            generator?.getCover(globalX, globalZ, this.random)
            generator?.getFiller(globalX, globalZ, this.random)
        }

        for (z in 0..15) {
            for (x in 0..15) {
                val index = x + z * 16
                val cover = this.coverBuffer[index]
                val filler = this.fillerBuffer[index]
                val noise = this.depthBuffer[index]
                this.generateBiomeTerrain(cover, filler, this.random, primer, globalX + x, globalZ + z, noise)
            }
        }

        if (this.settings.decorate) {
            IntCache.resetIntCache()

            generators.forEach {
                this.globGenerators[it]?.coverDecorate(primer, this.random, globalX, globalZ)
            }
        }
    }

    private fun generateBiomeTerrain(topBlock: IBlockState, fillerBlock: IBlockState, rand: Random, primer: ChunkPrimer, x: Int, z: Int, noise: Double) {
        val oceanHeight = this.handler.oceanHeight

        var currentTop = topBlock
        var currentFiller = fillerBlock
        var depth = -1
        val soilDepth = (noise / 3.0 + 3.0 + rand.nextDouble() * 0.25).toInt()
        val localX = x and 15
        val localZ = z and 15

        for (localY in 255 downTo 0) {
            if (localY <= rand.nextInt(5)) {
                primer.setBlockState(localX, localY, localZ, BEDROCK)
            } else {
                val current = primer.getBlockState(localX, localY, localZ)
                if (current.material === Material.AIR) {
                    depth = -1
                } else if (current.block === Blocks.STONE) {
                    if (depth == -1) {
                        if (soilDepth <= 0) {
                            currentTop = AIR
                            currentFiller = STONE
                        } else {
                            if (localY >= oceanHeight - 4 && localY <= oceanHeight + 1) {
                                currentTop = topBlock
                                currentFiller = fillerBlock
                            }
                        }
                        if (localY < oceanHeight && (currentTop == null || currentTop.material === Material.AIR)) {
                            currentTop = WATER
                        }
                        depth = soilDepth
                        if (localY >= oceanHeight - 1) {
                            primer.setBlockState(localX, localY, localZ, currentTop)
                        } else if (localY < oceanHeight - 7 - soilDepth) {
                            currentTop = AIR
                            currentFiller = STONE
                            primer.setBlockState(localX, localY, localZ, GRAVEL)
                        } else {
                            primer.setBlockState(localX, localY, localZ, currentFiller)
                        }
                    } else if (depth > 0) {
                        --depth
                        primer.setBlockState(localX, localY, localZ, currentFiller)
                    }
                }
            }
        }
    }

    override fun populate(chunkX: Int, chunkZ: Int) {
        BlockFalling.fallInstantly = true

        this.handler.getGlobRegion(this.globBuffer, chunkX, chunkZ)

        val x = chunkX shl 4
        val z = chunkZ shl 4

        this.random.setSeed(this.world.seed)
        val k = this.random.nextLong() / 2L * 2L + 1L
        val l = this.random.nextLong() / 2L * 2L + 1L
        this.random.setSeed(chunkX.toLong() * k + chunkZ.toLong() * l xor this.world.seed)

        val glob = this.globBuffer[255]
        val biome = glob.biome

        if (this.settings.decorate) {
            ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, chunkX, chunkZ, false)

            this.globGenerators[glob]?.decorate(this.random, x + 8, z + 8)

            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
                WorldEntitySpawner.performWorldGenSpawning(this.world, biome, x + 8, z + 8, 16, 16, this.random)
            }

            ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, chunkX, chunkZ, false)
        }

        BlockFalling.fallInstantly = false
    }

    override fun getPossibleCreatures(type: EnumCreatureType, pos: BlockPos): MutableList<Biome.SpawnListEntry> {
        return this.world.getBiome(pos).getSpawnableList(type)
    }

    override fun getNearestStructurePos(world: World, structureName: String, pos: BlockPos, map: Boolean) = pos

    override fun generateStructures(chunk: Chunk, x: Int, z: Int) = false

    override fun recreateStructures(chunk: Chunk, x: Int, z: Int) {
    }

    /**
     * isInsideStructure
     */
    override fun func_193414_a(world: World?, feature: String?, pos: BlockPos?): Boolean {
        return false
    }
}
