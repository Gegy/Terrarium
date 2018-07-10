package net.gegy1000.terrarium.client.render;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.ClientEventHandler;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.config.TerrariumConfig;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingState;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingStateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Terrarium.MODID, value = Side.CLIENT)
public class LoadingStateOverlay {
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(Terrarium.MODID, "textures/gui/widgets.png");
    private static final Minecraft MC = Minecraft.getMinecraft();

    public static void onRender(int mouseX, int mouseY) {
        if (TerrariumConfig.dataStatusIcon) {
            LoadingState state = LoadingStateHandler.getDisplayState();

            if (state != null) {
                MC.getTextureManager().bindTexture(WIDGETS_TEXTURE);

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                int animationTicks = ClientEventHandler.getGameTicks() % 40 / 5;
                int frame = animationTicks >= 5 ? 3 - (animationTicks - 5) : animationTicks;

                Gui.drawModalRectWithCustomSizedTexture(10, 10, frame * 10, state.getTextureY(), 10, 10, 256, 256);

                if (mouseX >= 10 && mouseY >= 10 && mouseX <= 20 && mouseY <= 20) {
                    String name = TextFormatting.WHITE + I18n.format(state.getLanguageKey() + ".name");
                    String tooltip = TextFormatting.GRAY + I18n.format(state.getLanguageKey() + ".tooltip");
                    GuiRenderUtils.drawTooltip(Lists.newArrayList(name, tooltip), mouseX, mouseY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        LoadingStateOverlay.onRender(event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void onOverlayRender(RenderGameOverlayEvent event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && MC.currentScreen == null) {
            GlStateManager.enableAlpha();
            LoadingStateOverlay.onRender(Integer.MIN_VALUE, Integer.MIN_VALUE);
            GlStateManager.disableAlpha();
        }
    }
}
