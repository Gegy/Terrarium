package net.gegy1000.terrarium.server.map.glob

import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.layer.GenLayer
import java.util.Random

abstract class GlobGenerator(val type: GlobType) {
    open val scatterScale: Double = 1.0

    var seed: Long = 0

    val topBlock = this.type.biome.topBlock
    val fillerBlock = this.type.biome.fillerBlock

    protected val pos = BlockPos.MutableBlockPos()

    protected lateinit var world: World
    protected lateinit var globBuffer: Array<GlobType>
    protected lateinit var heightBuffer: IntArray
    protected lateinit var coverBuffer: Array<IBlockState>
    protected lateinit var fillerBuffer: Array<IBlockState>

    fun initialize(world: World, globBuffer: Array<GlobType>, heightBuffer: IntArray, coverBuffer: Array<IBlockState>, fillerBuffer: Array<IBlockState>) {
        this.world = world
        this.seed = world.seed
        this.globBuffer = globBuffer
        this.heightBuffer = heightBuffer
        this.coverBuffer = coverBuffer
        this.fillerBuffer = fillerBuffer
        this.createLayers()
    }

    protected open fun createLayers() {
    }

    open fun decorate(random: Random, x: Int, z: Int) {
    }

    open fun coverDecorate(primer: ChunkPrimer, random: Random, x: Int, z: Int) {
    }

    open fun getCover(x: Int, z: Int, random: Random) {
        this.iterate { localX: Int, localZ: Int ->
            this.coverBuffer[localX + localZ * 16] = this.getCoverAt(x + localX, z + localZ, random)
        }
    }

    protected open fun getCoverAt(x: Int, z: Int, random: Random): IBlockState = this.topBlock

    open fun getFiller(x: Int, z: Int, random: Random) {
        this.iterate { localX: Int, localZ: Int ->
            this.fillerBuffer[localX + localZ * 16] = this.getFillerAt(x + localX, z + localZ, random)
        }
    }

    protected open fun getFillerAt(x: Int, z: Int, random: Random): IBlockState = this.fillerBlock

    protected fun <T> select(random: Random, vararg items: T) = items[random.nextInt(items.size)]

    protected fun range(random: Random, minimum: Int, maximum: Int) = random.nextInt(maximum - minimum) + minimum

    protected fun sampleChunk(layer: GenLayer, x: Int, z: Int) = layer.getInts(x, z, 16, 16)

    protected fun scatter(coordinate: Int, range: Int, random: Random) = coordinate + random.nextInt(range) - random.nextInt(range)

    protected fun scatterDecorate(x: Int, z: Int, random: Random): BlockPos {
        this.pos.setPos(x + random.nextInt(16), 0, z + random.nextInt(16))
        return this.pos
    }

    protected inline fun iterate(lambda: (localX: Int, localZ: Int) -> Unit) {
        for (localZ in 0..15) {
            for (localX in 0..15) {
                if (this.globBuffer[localX + localZ * 16] == this.type) {
                    lambda(localX, localZ)
                }
            }
        }
    }

    protected inline fun decorateScatter(x: Int, z: Int, count: Int, random: Random, lambda: (pos: BlockPos) -> Unit) {
        for (i in 0..count) {
            lambda(this.world.getTopSolidOrLiquidBlock(this.scatterDecorate(x, z, random)))
        }
    }

    protected inline fun coverLayer(buffer: Array<IBlockState>, x: Int, z: Int, layer: GenLayer, populate: (Int) -> IBlockState) {
        val sampled = this.sampleChunk(layer, x, z)
        this.iterate { localX: Int, localZ: Int ->
            val index = localX + localZ * 16
            buffer[index] = populate(sampled[index])
        }
    }
}
