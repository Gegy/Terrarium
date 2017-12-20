package net.gegy1000.terrarium.client

import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.config.TerrariumConfig
import net.gegy1000.terrarium.server.map.source.LoadingStateHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextFormatting.GRAY
import net.minecraft.util.text.TextFormatting.WHITE
import net.minecraft.util.text.translation.I18n
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.client.config.GuiUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object ClientEventHandler : Gui() {
    private val minecraft = Minecraft.getMinecraft()
    private val widgetsTexture = ResourceLocation(Terrarium.MODID, "textures/gui/widgets.png")

    private var ticks = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            ticks++
        }
    }

    @SubscribeEvent
    fun onGuiRender(event: GuiScreenEvent.DrawScreenEvent) {
        drawLoadingState(event.mouseX, event.mouseY)
    }

    @SubscribeEvent
    fun onOverlayRender(event: RenderGameOverlayEvent) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL && minecraft.currentScreen == null) {
            GlStateManager.enableAlpha()
            drawLoadingState(Int.MIN_VALUE, Int.MIN_VALUE)
            GlStateManager.disableAlpha()
        }
    }

    private fun drawLoadingState(mouseX: Int, mouseY: Int) {
        if (!TerrariumConfig.dataStatusIcon) {
            return
        }

        val state = LoadingStateHandler.checkState()

        if (state != null) {
            val resolution = ScaledResolution(minecraft)

            minecraft.textureManager.bindTexture(widgetsTexture)

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)

            val animationTicks = ticks % 40 / 5
            val frame = if (animationTicks >= 5) 3 - (animationTicks - 5) else animationTicks

            drawTexturedModalRect(10, 10, frame * 10, state.textureY, 10, 10)

            if (mouseX >= 10 && mouseY >= 10 && mouseX <= 20 && mouseY <= 20) {
                val lines = listOf(WHITE.toString() + I18n.translateToLocal("${state.languageKey}.name"), GRAY.toString() + I18n.translateToLocal("${state.languageKey}.tooltip"))
                GuiUtils.drawHoveringText(lines, mouseX, mouseY, resolution.scaledWidth, resolution.scaledHeight, -1, minecraft.fontRenderer)

                GlStateManager.disableLighting()
                GlStateManager.disableDepth()
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
            }
        }
    }
}
