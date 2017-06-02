package net.gegy1000.terrarium.server.world.generator

import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.world.chunk.ChunkPrimer

class OffsetChunkPrimer(private val parent: ChunkPrimer, private val offsetY: Int) : ChunkPrimer() {
    override fun getBlockState(x: Int, y: Int, z: Int): IBlockState {
        val offsetY = y - this.offsetY
        if (offsetY < 0) {
            return Blocks.BEDROCK.defaultState
        }
        return this.parent.getBlockState(x, offsetY, z)
    }

    override fun setBlockState(x: Int, y: Int, z: Int, state: IBlockState) {
        val offsetY = y - this.offsetY
        if (offsetY > 0) {
            this.parent.setBlockState(x, offsetY, z, state)
        }
    }

    override fun findGroundBlockIdx(x: Int, z: Int): Int {
        return this.parent.findGroundBlockIdx(x, z) - this.offsetY
    }
}