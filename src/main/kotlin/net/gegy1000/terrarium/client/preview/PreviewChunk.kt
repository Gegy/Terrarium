package net.gegy1000.terrarium.client.preview

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.client.render.TerrariumVertexFormats
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.WorldVertexBufferUploader
import net.minecraft.client.renderer.vertex.VertexBuffer
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumFacing.WEST
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.ChunkPrimer
import org.lwjgl.opengl.GL11
import java.util.concurrent.Future

class PreviewChunk(val chunk: ChunkPrimer, val pos: ChunkPos, private val previewState: PreviewState) {
    companion object {
        private val FACES = arrayOf(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST)
    }

    private val globalPos = ChunkPos(pos.x shl 4, pos.z shl 4)

    private var builderResult: Future<BufferBuilder?>? = previewState.executor.submit<BufferBuilder?> {
        val builder = previewState.takeBuilder()
        buildBlocks(builder)
        builder
    }

    private var geometry: Geometry? = null

    fun performUpload() {
        builderResult?.let { result ->
            if (result.isDone) {
                try {
                    val builder = result.get()

                    geometry?.delete()
                    geometry = buildGeometry(builder)

                    builder?.let(previewState::resetBuilder)

                    builderResult = null
                } catch (e: Exception) {
                    Terrarium.LOGGER.error("Failed to generate preview chunk geometry at $pos", e)
                }
            }
        }
    }

    fun render() {
        geometry?.render()
    }

    fun delete() {
        geometry?.delete()
    }

    private fun buildGeometry(builder: BufferBuilder?): Geometry {
        if (builder == null || builder.vertexCount == 0) {
            builder?.finishDrawing()
            return EmptyGeometry()
        }
        return buildDisplayList(builder)
    }

    private fun buildVbo(builder: BufferBuilder): VboGeometry {
        val buffer = VertexBuffer(TerrariumVertexFormats.POSITION_COLOR_NORMAL)

        builder.finishDrawing()
        buffer.bufferData(builder.byteBuffer)

        return VboGeometry(buffer)
    }

    private fun buildDisplayList(builder: BufferBuilder): DisplayListGeometry {
        val id = GLAllocation.generateDisplayLists(1)

        builder.finishDrawing()
        GlStateManager.glNewList(id, GL11.GL_COMPILE)
        WorldVertexBufferUploader().draw(builder)
        GlStateManager.glEndList()

        return DisplayListGeometry(id)
    }

    private fun buildBlocks(builder: BufferBuilder) {
        builder.begin(GL11.GL_QUADS, TerrariumVertexFormats.POSITION_COLOR_NORMAL)

        val pos = MutableBlockPos()
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..255) {
                    val state = chunk.getBlockState(x, y, z)
                    if (state.block !== Blocks.AIR) {
                        pos.setPos(globalPos.x + x, y, globalPos.z + z)

                        val faces = FACES.filter { facing ->
                            val offset = pos.offset(facing)
                            val neighbourState = if (x in 1..14 && z in 1..14 && y in 1..254) {
                                chunk.getBlockState(offset.x and 15, offset.y, offset.z and 15)
                            } else {
                                previewState.getBlockState(offset)
                            }
                            neighbourState.block === Blocks.AIR
                        }

                        if (!faces.isEmpty()) {
                            renderFaces(faces, state, pos, builder)
                        }
                    }
                }
            }
        }

        builder.setTranslation(0.0, 0.0, 0.0)
    }

    private fun renderFaces(faces: List<EnumFacing>, state: IBlockState, pos: MutableBlockPos, builder: BufferBuilder) {
        val color = state.getMapColor(previewState, pos)
        val red = (color.colorValue shr 16) and 0xFF
        val green = (color.colorValue shr 8) and 0xFF
        val blue = color.colorValue and 0xFF

        builder.setTranslation(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

        faces.forEach { buildFace(builder, it, red, green, blue) }
    }

    private fun buildFace(builder: BufferBuilder, facing: EnumFacing, red: Int, green: Int, blue: Int) {
        when (facing) {
            NORTH -> {
                builder.pos(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex()
                builder.pos(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex()
                builder.pos(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex()
                builder.pos(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex()
            }
            SOUTH -> {
                builder.pos(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex()
                builder.pos(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex()
                builder.pos(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex()
                builder.pos(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex()
            }
            WEST -> {
                builder.pos(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex()
                builder.pos(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex()
                builder.pos(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex()
                builder.pos(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex()
            }
            EAST -> {
                builder.pos(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex()
                builder.pos(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex()
                builder.pos(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex()
                builder.pos(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex()
            }
            UP -> {
                builder.pos(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
                builder.pos(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
                builder.pos(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
                builder.pos(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
            }
            DOWN -> {
                builder.pos(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
                builder.pos(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
                builder.pos(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
                builder.pos(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex()
            }
        }
    }

    interface Geometry {
        fun render()

        fun delete()
    }

    class VboGeometry(private val buffer: VertexBuffer) : Geometry {
        override fun render() {
            buffer.bindBuffer()

            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0)
            GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12)
            // TODO: Bind for nomal

            buffer.drawArrays(GL11.GL_QUADS)
            buffer.unbindBuffer()
        }

        override fun delete() {
            buffer.deleteGlBuffers()
        }
    }

    class DisplayListGeometry(private val id: Int) : Geometry {
        override fun render() {
            GlStateManager.callList(id)
        }

        override fun delete() {
            GLAllocation.deleteDisplayLists(id)
        }
    }

    class EmptyGeometry : Geometry {
        override fun render() = Unit

        override fun delete() = Unit
    }
}
