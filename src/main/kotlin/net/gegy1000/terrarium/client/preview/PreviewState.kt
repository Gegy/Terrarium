package net.gegy1000.terrarium.client.preview

import net.gegy1000.terrarium.server.util.Coordinate
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.EarthWorldType
import net.gegy1000.terrarium.server.world.generator.EarthChunkGenerator
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.init.Biomes
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.chunk.ChunkPrimer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService

class PreviewState(val settings: EarthGenerationSettings, val executor: ExecutorService, private val builders: BlockingQueue<BufferBuilder>) : IBlockAccess {
    val viewChunkRange = 16

    val generator = EarthChunkGenerator(PreviewDummyWorld(), 0, settings.serialize(), true)

    val centerPos = ChunkPos(Coordinate.fromLatLng(settings, settings.spawnLatitude, settings.spawnLongitude).toBlockPos())
    val centerBlockPos = BlockPos(this.centerPos.x shl 4, 0, this.centerPos.z shl 4)

    private val chunkMap = HashMap<ChunkPos, ChunkPrimer>()

    private var previewChunks: List<PreviewChunk>? = null

    var heightOffset = 96

    init {
        executor.submit {
            var maxHeight = 96

            for (z in -viewChunkRange..viewChunkRange) {
                for (x in -viewChunkRange..viewChunkRange) {
                    val pos = ChunkPos(centerPos.x + x, centerPos.z + z)
                    val chunk = generator.generatePrimer(pos.x, pos.z)
                    chunkMap.put(pos, chunk)
                }
            }

            previewChunks = chunkMap.toList().sortedBy { (pos, _) ->
                val deltaX = pos.x - centerPos.x
                val deltaZ = pos.z - centerPos.z
                deltaX * deltaX + deltaZ * deltaZ
            }.map { (pos, chunk) ->
                val chunkHeight = chunk.findGroundBlockIdx(8, 8) + 16
                if (chunkHeight > maxHeight) {
                    maxHeight = chunkHeight
                }
                PreviewChunk(chunk, pos, this)
            }

            heightOffset = maxHeight
        }
    }

    fun render() {
        previewChunks?.let { chunks ->
            val startTime = System.nanoTime()
            for (chunk in chunks) {
                chunk.performUpload()
                if (System.nanoTime() - startTime > 5000000) {
                    break
                }
            }

            chunks.forEach(PreviewChunk::render)
        }
    }

    fun delete() {
        previewChunks?.forEach(PreviewChunk::delete)
    }

    fun takeBuilder() = builders.take()

    fun resetBuilder(builder: BufferBuilder) {
        builders.add(builder)
    }

    override fun isSideSolid(pos: BlockPos, side: EnumFacing?, _default: Boolean) = getBlockState(pos).isFullCube

    override fun isAirBlock(pos: BlockPos): Boolean {
        val state = getBlockState(pos)
        return state.block.isAir(state, this, pos)
    }

    override fun getStrongPower(pos: BlockPos, direction: EnumFacing?) = 0

    override fun getCombinedLight(pos: BlockPos, lightValue: Int) = lightValue

    override fun getTileEntity(pos: BlockPos) = null

    override fun getBlockState(pos: BlockPos): IBlockState {
        if (pos.y > 255 || pos.y < 0) {
            return Blocks.AIR.defaultState
        }
        val chunk = chunkMap[ChunkPos(pos.x shr 4, pos.z shr 4)]
        return chunk?.getBlockState(pos.x and 15, pos.y, pos.z and 15) ?: Blocks.STONE.defaultState
    }

    override fun getBiome(pos: BlockPos) = Biomes.DEFAULT

    override fun getWorldType() = EarthWorldType
}
