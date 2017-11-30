package net.gegy1000.terrarium.client.gui

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.client.preview.PreviewChunk
import net.gegy1000.terrarium.client.preview.PreviewWorld
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.gegy1000.terrarium.server.world.EarthWorldType
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.text.translation.I18n
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import kotlin.concurrent.thread

@SideOnly(Side.CLIENT)
class CustomizeEarthGUI(val parent: GuiScreen) : GuiScreen() {
    val title = I18n.translateToLocal("options.${Terrarium.MODID}:customize_earth_title.name")
    val settings = EarthGenerationSettings()

    val previewWorld = PreviewWorld()
    val previewChunks = ArrayList<PreviewChunk>()

    var rotationX: Float = 45.0F

    var lastMouseX: Int = 0

    var heightOffset = 128

    var setup = false

    var mouseDown = false

    init {
        thread(name = "Preview Chunk Load Thread", start = true, isDaemon = true) {
            val range = previewWorld.viewChunkRange

            val centerPos = previewWorld.centerPos

            for (x in -range..range) {
                for (z in -range..range) {
                    val chunk = previewWorld.getChunkFromChunkCoords(centerPos.x + x, centerPos.z + z)

                    for (height in chunk.heightMap) {
                        if (height > heightOffset) {
                            heightOffset = height
                        }
                    }

                    for (y in (chunk.topFilledSegment shr 4) downTo 0) {
                        previewChunks.add(PreviewChunk(previewWorld, chunk, centerPos.x + x, y, centerPos.z + z))
                    }
                }
            }

            previewChunks.sortBy { Math.abs((it.x - previewWorld.centerPos.x) * (it.z - previewWorld.centerPos.z)) }
            previewChunks.sortByDescending { it.y }

            setup = true
        }
    }

    override fun initGui() {
        this.resetWorld()
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.enabled) {

        }

        // TODO: remove
        this.resetWorld()
    }

    override fun updateScreen() {
        super.updateScreen()

        if (!this.mouseDown) {
            this.rotationX += 0.25F
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        this.mouseDown = true
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
        this.mouseDown = false
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.lastMouseX = mouseX

        this.drawDefaultBackground()
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 4, 0xFFFFFF)
        super.drawScreen(mouseX, mouseY, partialTicks)
        this.drawPreview(mouseX, mouseY, partialTicks)
    }

    private fun drawPreview(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val resolution = ScaledResolution(this.mc)

        val padding = 14.0
        val width = this.width - padding * 2
        val height = this.height / 2 - padding * 2
        val x = padding
        val y = this.height - height - padding
        this.drawPreviewBackground(width, height, x, y)

        if (this.setup) {
            this.drawPreviewWorld(resolution, x, y, width, height)
        }
    }

    private fun drawPreviewWorld(resolution: ScaledResolution, x: Double, y: Double, width: Double, height: Double) {
        this.performBuild()

        GlStateManager.pushMatrix()

        val scaleFactor = resolution.scaleFactor.toDouble()
        GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor)

        val center = this.previewWorld.centerBlockPos

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        this.scissor(x, y, width, height)

        val scale = 0.3F
        GlStateManager.translate(this.width / 2 / scaleFactor, y / scaleFactor + this.heightOffset * scale, 0.0)
        GlStateManager.scale(scale, -scale, scale)
        GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F)
        GlStateManager.rotate(this.rotationX, 0.0F, 1.0F, 0.0F)

        GlStateManager.translate(-center.x.toFloat(), 0.0F, -center.z.toFloat())

        this.mc.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

        this.enablePreviewState()

        GlStateManager.enableDepth()

        this.renderPreviewLayer(BlockRenderLayer.SOLID)

        GlStateManager.enableAlpha()

        this.renderPreviewLayer(BlockRenderLayer.CUTOUT_MIPPED)

        this.mc.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false)
        this.renderPreviewLayer(BlockRenderLayer.CUTOUT)

        this.mc.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap()
        this.renderPreviewLayer(BlockRenderLayer.TRANSLUCENT)

        GlStateManager.disableAlpha()
        GlStateManager.disableDepth()

        this.disablePreviewState()

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GlStateManager.popMatrix()
    }

    private fun scissor(x: Double, y: Double, width: Double, height: Double) {
        val scaleFactor = ScaledResolution(this.mc).scaleFactor
        GL11.glScissor((x * scaleFactor).toInt(), ((this.height - (y + height)) * scaleFactor).toInt(), (width * scaleFactor).toInt(), (height * scaleFactor).toInt())
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)

        this.rotationX += (mouseX - this.lastMouseX) * 0.5F
    }

    private fun renderPreviewLayer(layer: BlockRenderLayer) {
        for (chunk in this.previewChunks) {
            chunk.renderLayer(layer)
        }
    }

    private fun drawPreviewBackground(width: Double, height: Double, x: Double, y: Double) {
        this.mc.textureManager.bindTexture(Gui.OPTIONS_BACKGROUND)
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        val tileSize = 32.0
        GlStateManager.color(0.125F, 0.125F, 0.125F, 1.0F)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        buffer.pos(x, y + height, 0.0).tex(x / tileSize, (y + height) / tileSize).endVertex()
        buffer.pos(x + width, y + height, 0.0).tex((x + width) / tileSize, (y + height) / tileSize).endVertex()
        buffer.pos(x + width, y, 0.0).tex((x + width) / tileSize, y / tileSize).endVertex()
        buffer.pos(x, y, 0.0).tex(x / tileSize, y / tileSize).endVertex()
        tessellator.draw()
    }

    private fun performBuild() {
        val ambientOcclusion = this.mc.gameSettings.ambientOcclusion
        this.mc.gameSettings.ambientOcclusion = 0
        var totalTime = 0L
        for (chunk in this.previewChunks) {
            val startTime = System.currentTimeMillis()
            if (chunk.checkDirty()) {
                totalTime += System.currentTimeMillis() - startTime
            }
            if (totalTime > 50) {
                break
            }
        }
        this.mc.gameSettings.ambientOcclusion = ambientOcclusion
    }

    private fun enablePreviewState() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY)
    }

    private fun disablePreviewState() {
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY)
    }

    private fun resetWorld() {
        val settings = EarthGenerationSettings.deserialize("")
        this.previewWorld.updateGenerator(settings, EarthWorldType.getChunkGenerator(this.previewWorld, settings.serialize()))

        this.previewChunks.forEach(PreviewChunk::markDirty)
    }

    override fun onGuiClosed() {
        super.onGuiClosed()

        this.previewChunks.forEach(PreviewChunk::delete)
    }
}
