package net.gegy1000.terrarium.client.preview

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import org.lwjgl.opengl.GL11

class PreviewChunk(val world: PreviewWorld, val chunk: Chunk, val x: Int, val y: Int, val z: Int) {
    val renderDispatcher = Minecraft.getMinecraft().blockRendererDispatcher

    val layers = Array(BlockRenderLayer.values().size, { Layer(this, BlockRenderLayer.values()[it]) })

    val empty: Boolean
        get() = this.storage == null || this.storage.isEmpty

    val globalX = this.x shl 4
    val globalY = this.y shl 4
    val globalZ = this.z shl 4

    val storage = this.chunk.blockStorageArray[this.y]

    fun renderLayer(blockLayer: BlockRenderLayer) {
        if (!this.empty) {
            this.layers[blockLayer.ordinal].render()
        }
    }

    fun checkDirty(): Boolean {
        if (!this.empty) {
            return this.layers.any { it.checkDirty() }
        }
        return false
    }

    private fun bindAttributes() {
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0)
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12)
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    fun delete() {
        this.layers.forEach { it.delete() }
    }

    fun markDirty() {
        this.layers.forEach { it.dirty = true }
    }

    override fun equals(other: Any?): Boolean {
        if (other is PreviewChunk) {
            return other.x == this.x && other.y == this.y && other.z == this.z
        }
        return false
    }

    override fun hashCode(): Int {
        return this.x shl 12 or this.z shl 4 or this.y
    }

    class Layer(val chunk: PreviewChunk, val layer: BlockRenderLayer) {
        var builder: BufferBuilder? = null
        var buffer: VertexBuffer? = null

        var dirty = false

        fun render() {
            val buffer = this.buffer
            buffer?.let {
                buffer.bindBuffer()
                this.chunk.bindAttributes()
                buffer.drawArrays(GL11.GL_QUADS)
                buffer.unbindBuffer()
            }
        }

        fun checkDirty(): Boolean {
            val builder = this.builder

            if (builder != null) {
                this.buffer?.deleteGlBuffers()

                val buffer = VertexBuffer(DefaultVertexFormats.BLOCK)

                buffer.bindBuffer()
                this.chunk.bindAttributes()
                buffer.unbindBuffer()

                buffer.bufferData(builder.byteBuffer)

                this.buffer = buffer
                this.builder = null

                return true
            } else if (this.dirty) {
                this.rebuildLayer()

                return true
            }

            return false
        }

        fun rebuildLayer() {
            // TODO: Create dedicated thread for chunk building
            this.dirty = false
            launch(CommonPool) { buildAsync() }
        }

        private suspend fun buildAsync() {
            val builder = BufferBuilder(0x4000)
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK)

            val pos = BlockPos.MutableBlockPos()

            val globalX = this.chunk.globalX
            val globalY = this.chunk.globalY
            val globalZ = this.chunk.globalZ

            for (x in 0..15) {
                for (z in 0..15) {
                    for (y in 0..15) {
                        val state = this.chunk.storage[x, y, z]
                        if (state.block.canRenderInLayer(state, this.layer)) {
                            pos.setPos(globalX + x, globalY + y, globalZ + z)
                            this.chunk.renderDispatcher.renderBlock(state, pos, this.chunk.world, builder)
                        }
                    }
                }
            }

            builder.finishDrawing()

            this.builder = builder
        }

        fun delete() {
            this.buffer?.deleteGlBuffers()
            this.buffer = null
        }
    }
}
