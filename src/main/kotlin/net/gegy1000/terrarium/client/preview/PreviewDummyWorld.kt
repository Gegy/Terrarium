package net.gegy1000.terrarium.client.preview

import net.gegy1000.terrarium.server.world.EarthWorldType
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.profiler.Profiler
import net.minecraft.world.GameType
import net.minecraft.world.World
import net.minecraft.world.WorldProvider
import net.minecraft.world.WorldProviderSurface
import net.minecraft.world.WorldSettings
import net.minecraft.world.chunk.EmptyChunk
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.storage.ISaveHandler
import net.minecraft.world.storage.WorldInfo

class PreviewDummyWorld : World(
        SaveHandler,
        WorldInfo(WorldSettings(0, GameType.ADVENTURE, false, false, EarthWorldType), "terrarium_preview"),
        WorldProviderSurface(),
        Profiler(),
        false
) {

    val generator: IChunkGenerator

    init {
        val dimension = this.provider.dimension
        this.provider.setWorld(this)
        this.provider.dimension = dimension
        this.generator = this.provider.createChunkGenerator()
        this.chunkProvider = this.createChunkProvider()

        initCapabilities()
    }

    override fun createChunkProvider() = ChunkCache(this)

    override fun isChunkLoaded(x: Int, z: Int, allowEmpty: Boolean) = false

    class ChunkCache(private val world: World) : IChunkProvider {
        override fun tick() = false

        override fun makeString() = "PreviewDummyWorld.ChunkCache"

        override fun getLoadedChunk(x: Int, z: Int) = null

        override fun provideChunk(x: Int, z: Int) = EmptyChunk(world, x, z)

        override fun isChunkGeneratedAt(x: Int, z: Int) = false
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
