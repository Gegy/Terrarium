package net.gegy1000.terrarium.client.preview

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthWorldType
import net.gegy1000.terrarium.server.world.generator.EarthChunkGenerator
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.profiler.Profiler
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.GameType
import net.minecraft.world.World
import net.minecraft.world.WorldProvider
import net.minecraft.world.WorldProviderSurface
import net.minecraft.world.WorldSettings
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.EmptyChunk
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.storage.ISaveHandler
import net.minecraft.world.storage.WorldInfo

val worldSettings = WorldSettings(0, GameType.ADVENTURE, false, false, EarthWorldType)
val profiler = Profiler()

class PreviewWorld : World(SaveHandler, WorldInfo(worldSettings, "terrarium_preview"), WorldProviderSurface(), profiler, false) {
    val viewChunkRange = 8
    val viewRange = this.viewChunkRange shl 4

    var generator: EarthChunkGenerator

    val centerPos: ChunkPos
        get() = ChunkPos(Coordinate.fromLatLng(this.generator.settings,27.988350, 86.923641).toBlockPos())

    val centerBlockPos: BlockPos
        get() = BlockPos(this.centerPos.x shl 4, 0, this.centerPos.z shl 4)

    init {
        val dimension = this.provider.dimension
        this.provider.setWorld(this)
        this.provider.dimension = dimension
        this.generator = this.provider.createChunkGenerator() as EarthChunkGenerator
        this.chunkProvider = this.createChunkProvider()
    }

    override fun createChunkProvider() = ChunkCache(this.generator)

    override fun getBlockState(pos: BlockPos): IBlockState {
        if (this.inRange(pos)) {
            return super.getBlockState(pos)
        }
        return Blocks.AIR.defaultState
    }

    override fun getChunkFromChunkCoords(chunkX: Int, chunkZ: Int): Chunk {
        if (this.chunkInRange(chunkX, chunkZ)) {
            return super.getChunkFromChunkCoords(chunkX, chunkZ)
        }
        return EmptyChunk(this, chunkX, chunkZ)
    }

    override fun isChunkLoaded(x: Int, z: Int, allowEmpty: Boolean) = this.chunkProvider.isChunkGeneratedAt(x, z)

    private fun inRange(pos: BlockPos): Boolean {
        val subtract = pos.subtract(this.centerBlockPos)
        val range = -this.viewRange..this.viewRange + 16
        return subtract.x in range && subtract.z in range
    }

    private fun chunkInRange(x: Int, z: Int): Boolean {
        val range = -this.viewChunkRange..this.viewChunkRange
        return x - this.centerPos.x in range && z - this.centerPos.z in range
    }

    fun updateGenerator(generator: EarthChunkGenerator) {
        this.generator = generator
        val cache = this.chunkProvider as ChunkCache
        cache.generator = generator
        cache.clearChunks()
    }

    class ChunkCache(var generator: IChunkGenerator) : IChunkProvider {
        val chunks = Long2ObjectOpenHashMap<Chunk>(8192)

        override fun tick() = false

        override fun makeString() = "PreviewWorld.ChunkCache: ${this.chunks.size}"

        override fun getLoadedChunk(x: Int, z: Int) = this.chunks[ChunkPos.asLong(x, z)]

        override fun provideChunk(x: Int, z: Int): Chunk {
            val chunk = this.getLoadedChunk(x, z)

            if (chunk == null) {
                val generatedChunk = this.generator.generateChunk(x, z)
                generatedChunk.onLoad()
                generatedChunk.populate(this, this.generator)

                this.chunks.put(ChunkPos.asLong(x, z), generatedChunk)

                return generatedChunk
            }

            return chunk
        }

        override fun isChunkGeneratedAt(x: Int, z: Int) = this.chunks.containsKey(ChunkPos.asLong(x, z))

        fun clearChunks() {
            this.chunks.clear()
        }
    }

    object SaveHandler : ISaveHandler {
        override fun checkSessionLock() {
        }

        override fun saveWorldInfoWithPlayer(worldInformation: WorldInfo?, tagCompound: NBTTagCompound?) {
        }

        override fun flush() {
        }

        override fun saveWorldInfo(worldInformation: WorldInfo?) {
        }

        override fun getPlayerNBTManager() = null

        override fun getMapFileFromName(mapName: String?) = null

        override fun loadWorldInfo() = null

        override fun getWorldDirectory() = null

        override fun getStructureTemplateManager() = null

        override fun getChunkLoader(provider: WorldProvider?) = null
    }
}
