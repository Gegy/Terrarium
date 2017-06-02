package net.gegy1000.terrarium.client.gui

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.world.EarthGenerationSettings
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.text.translation.I18n
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
class CustomizeEarthGUI(val parent: GuiScreen) : GuiScreen() {
    val title = I18n.translateToLocal("options.${Terrarium.MODID}:customize_earth_title.name")
    val settings = EarthGenerationSettings()

    override fun initGui() {
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.enabled) {

        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawDefaultBackground()
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 4, 0xFFFFFF)
        super.drawScreen(mouseX, mouseY, partialTicks)
        this.drawPreview(mouseX, mouseY, partialTicks)
    }

    private fun drawPreview(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val padding = 14.0
        val width = this.width - padding * 2
        val height = this.height / 2 - padding * 2
        val x = padding
        val y = this.height - height - padding
        this.drawPreviewBackground(width, height, x, y)
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
}