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

    val pos = BlockPos.MutableBlockPos()

    fun initialize(world: World) {
        this.seed = world.seed
        this.createLayers(world)
    }

    protected open fun createLayers(world: World) {
    }

    open fun decorate(world: World, random: Random, x: Int, z: Int) {
    }

    open fun coverDecorate(globBuffer: Array<GlobType>, heightBuffer: IntArray, primer: ChunkPrimer, random: Random, x: Int, z: Int) {
    }

    open fun getCover(glob: Array<GlobType>, cover: Array<IBlockState>, x: Int, z: Int, random: Random) {
        this.foreach(glob) { localX: Int, localZ: Int ->
            cover[localX + localZ * 16] = this.getCover(x + localX, z + localZ, random)
        }
    }

    protected open fun getCover(x: Int, z: Int, random: Random): IBlockState = this.topBlock

    open fun getFiller(glob: Array<GlobType>, filler: Array<IBlockState>, x: Int, z: Int, random: Random) {
        this.foreach(glob) { localX: Int, localZ: Int ->
            filler[localX + localZ * 16] = this.getFiller(x + localX, z + localZ, random)
        }
    }

    protected open fun getFiller(x: Int, z: Int, random: Random): IBlockState = this.fillerBlock

    protected fun <T> select(random: Random, vararg items: T) = items[random.nextInt(items.size)]

    protected fun range(random: Random, minimum: Int, maximum: Int) = random.nextInt(maximum - minimum) + minimum

    protected fun sampleChunk(layer: GenLayer, x: Int, z: Int) = layer.getInts(x, z, 16, 16)

    protected fun scatter(coordinate: Int, range: Int, random: Random) = coordinate + random.nextInt(range) - random.nextInt(range)

    protected fun scatterDecorate(x: Int, z: Int, random: Random): BlockPos {
        this.pos.setPos(x + random.nextInt(16), 0, z + random.nextInt(16))
        return this.pos
    }

    protected inline fun foreach(buffer: Array<GlobType>, lambda: (localX: Int, localZ: Int) -> Unit) {
        for (localZ in 0..15) {
            for (localX in 0..15) {
                if (buffer[localX + localZ * 16] == this.type) {
                    lambda(localX, localZ)
                }
            }
        }
    }

    protected inline fun decorateScatter(x: Int, z: Int, count: Int, world: World, random: Random, lambda: (pos: BlockPos) -> Unit) {
        for (i in 0..count) {
            lambda(world.getTopSolidOrLiquidBlock(this.scatterDecorate(x, z, random)))
        }
    }
}
