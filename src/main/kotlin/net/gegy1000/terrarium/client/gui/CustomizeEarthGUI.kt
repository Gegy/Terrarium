package net.gegy1000.terrarium.client.gui

import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.client.preview.PreviewState
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.translation.I18n
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors

@SideOnly(Side.CLIENT)
class CustomizeEarthGUI(val parent: GuiScreen) : GuiScreen() {
    val title = I18n.translateToLocal("options.${Terrarium.MODID}:customize_earth_title.name")
    val settings = EarthGenerationSettings()

    val executor = Executors.newFixedThreadPool(3, ThreadFactoryBuilder().setDaemon(true).setNameFormat("terrarium-preview-%d").build())
    private val builders = ArrayBlockingQueue<BufferBuilder>(8)

    init {
        for (i in 0 until 8) {
            builders.add(BufferBuilder(0x4000))
        }
    }

    private var rotationX = 45.0F
    private var lastRotationX = rotationX

    private var lastMouseX = 0

    private var zoom = 0.3F
    private var lastZoom = zoom

    private var mouseDown = false

    private var previewState: PreviewState? = null

    override fun initGui() {
        this.rebuildState()
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.enabled) {

        }
    }

    override fun updateScreen() {
        super.updateScreen()

        lastRotationX = rotationX
        lastZoom = zoom

        val scroll = Mouse.getDWheel()
        zoom = MathHelper.clamp(zoom + scroll / 1600.0F, 0.3F, 1.0F)

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

        this.drawPreviewWorld(resolution, x, y, width, height, partialTicks)
    }

    private fun drawPreviewWorld(resolution: ScaledResolution, x: Double, y: Double, width: Double, height: Double, partialTicks: Float) = previewState?.let { state ->
        GlStateManager.pushMatrix()

        val scaleFactor = resolution.scaleFactor.toDouble()
        GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor)

        val center = state.centerBlockPos

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        this.scissor(x, y, width, height)

        GlStateManager.enableRescaleNormal()
        val scale = lastZoom + (zoom - lastZoom) * partialTicks
        GlStateManager.translate(this.width / 2 / scaleFactor, y / scaleFactor, 0.0)
        GlStateManager.scale(scale, -scale, scale)
        GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F)
        GlStateManager.rotate(lastRotationX + (rotationX - lastRotationX) * partialTicks, 0.0F, 1.0F, 0.0F)

        GlStateManager.translate(-center.x.toFloat(), -state.heightOffset.toFloat(), -center.z.toFloat())
        GlStateManager.disableTexture2D()
        RenderHelper.enableStandardItemLighting()

//          this.enablePreviewState() TODO: Handle VBOs

        GlStateManager.enableDepth()

        state.render()

        GlStateManager.disableDepth()

        this.disablePreviewState()

        GlStateManager.disableRescaleNormal()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableTexture2D()
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

    private fun enablePreviewState() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY)
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY)
    }

    private fun disablePreviewState() {
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY)
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY)
    }

    private fun rebuildState() {
        previewState?.delete()
        previewState = PreviewState(settings, executor, builders)
    }

    override fun onGuiClosed() {
        super.onGuiClosed()

        executor.shutdownNow()
        previewState?.delete()
    }
}
