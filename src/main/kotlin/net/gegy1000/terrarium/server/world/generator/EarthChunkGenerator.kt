package net.gegy1000.terrarium.server.world.generator

import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.block.BlockFalling
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldEntitySpawner
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.chunk.IChunkGenerator
import net.minecraft.world.gen.NoiseGeneratorPerlin
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.terraingen.PopulateChunkEvent
import net.minecraftforge.event.terraingen.TerrainGen
import java.util.*

class EarthChunkGenerator(val world: World, seed: Long, settingsString: String) : IChunkGenerator {
    companion object {
        private val BEDROCK = Blocks.BEDROCK.defaultState
        private val STONE = Blocks.STONE.defaultState
        private val LIQUID = Blocks.WATER.defaultState
    }

    val random = Random(seed)
    val settings = EarthGenerationSettings.deserialize(settingsString)
    val handler = EarthGenerationHandler(this.settings)
    val surfaceNoise = NoiseGeneratorPerlin(this.random, 4)
    val pos = BlockPos.MutableBlockPos()
    var depthBuffer = DoubleArray(256)
    var biomeBuffer: Array<Biome>? = null

    override fun provideChunk(chunkX: Int, chunkZ: Int): Chunk {
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
        this.biomeBuffer = this.world.biomeProvider.getBiomesForGeneration(this.biomeBuffer, (chunkX shl 2) - 2, (chunkZ shl 2) - 2, 10, 10)

        val globalX = chunkX shl 4
        val globalZ = chunkZ shl 4

        val oceanHeight = this.handler.oceanHeight

        for (z in 0..15) {
            for (x in 0..15) {
                val height = this.handler.provideHeight(globalX + x, globalZ + z)
                for (y in 1..height) {
                    primer.setBlockState(x, y, z, STONE)
                }
                primer.setBlockState(x, 0, z, BEDROCK)
                if (height < oceanHeight) {
                    for (y in height + 1..oceanHeight) {
                        primer.setBlockState(x, y, z, LIQUID)
                    }
                }
            }
        }

        if (this.settings.mapFeatures) {

        }
    }

    private fun generateBiome(primer: ChunkPrimer, chunkX: Int, chunkZ: Int) {
        val offsetPrimer = OffsetChunkPrimer(primer, 62 - this.handler.oceanHeight)
        val scale = 0.03125
        this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, (chunkX shl 4).toDouble(), (chunkZ shl 4).toDouble(), 16, 16, scale * 2.0, scale * 2.0, 1.0)
        val buffer = this.biomeBuffer ?: return
        for (x in 0..15) {
            for (z in 0..15) {
                val biome = buffer[z + x * 16]
                val noise = this.depthBuffer[z + x * 16]
                biome.genTerrainBlocks(this.world, this.random, offsetPrimer, chunkX * 16 + x, chunkZ * 16 + z, noise)
            }
        }
    }

    override fun populate(chunkX: Int, chunkZ: Int) {
        BlockFalling.fallInstantly = true

        val x = chunkX shl 4
        val z = chunkZ shl 4

        this.random.setSeed(this.world.seed)
        val k = this.random.nextLong() / 2L * 2L + 1L
        val l = this.random.nextLong() / 2L * 2L + 1L
        this.random.setSeed(chunkX.toLong() * k + chunkZ.toLong() * l xor this.world.seed)

        this.pos.setPos(x + 16, 0, z + 16)
        val biome = this.world.getBiome(this.pos)

        this.pos.setPos(x, 0, z)

        if (this.settings.decorate) {
            ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, chunkX, chunkZ, false)

            biome.decorate(this.world, this.random, this.pos)

            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
                WorldEntitySpawner.performWorldGenSpawning(this.world, biome, x + 8, z + 8, 16, 16, this.random)
            }

            this.pos.setPos(x + 8, 0, z + 8)

            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.ICE)) {
                for (offsetX in 0..15) {
                    for (offsetZ in 0..15) {
                        val snowPos = this.world.getPrecipitationHeight(pos.add(offsetX, 0, offsetZ))
                        val groundPos = snowPos.down()

                        if (this.world.canBlockFreezeWater(groundPos)) {
                            this.world.setBlockState(groundPos, Blocks.ICE.defaultState, 2)
                        }

                        if (this.world.canSnowAt(snowPos, true)) {
                            this.world.setBlockState(snowPos, Blocks.SNOW_LAYER.defaultState, 2)
                        }
                    }
                }
            }

            ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, chunkX, chunkZ, false)
        }

        BlockFalling.fallInstantly = false
    }

    override fun getPossibleCreatures(type: EnumCreatureType, pos: BlockPos): MutableList<Biome.SpawnListEntry> {
        return this.world.getBiome(pos).getSpawnableList(type)
    }

    override fun getStrongholdGen(world: World, structureName: String, pos: BlockPos, map: Boolean) = pos

    override fun generateStructures(chunk: Chunk, x: Int, z: Int) = false

    override fun recreateStructures(chunk: Chunk, x: Int, z: Int) {
    }
}
