package net.gegy1000.terrarium.client;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.RemoteDataWarningGui;
import net.gegy1000.terrarium.server.config.TerrariumConfig;
import net.gegy1000.terrarium.server.map.source.LoadingState;
import net.gegy1000.terrarium.server.map.source.LoadingStateHandler;
import net.gegy1000.terrarium.server.world.EarthWorldType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Terrarium.MODID, value = Side.CLIENT)
public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(Terrarium.MODID, "textures/gui/widgets.png");

    private static int ticks = 0;

    private static boolean awaitingLoad;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ticks++;

            if (awaitingLoad && MC.player != null && MC.player.ticksExisted > 10) {
                awaitingLoad = false;
                if (!TerrariumConfig.acceptedRemoteDataWarning) {
                    MC.displayGuiScreen(new RemoteDataWarningGui(MC.currentScreen));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGuiRender(GuiScreenEvent.DrawScreenEvent event) {
        drawLoadingState(event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void onOverlayRender(RenderGameOverlayEvent event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && MC.currentScreen == null) {
            GlStateManager.enableAlpha();
            drawLoadingState(Integer.MIN_VALUE, Integer.MIN_VALUE);
            GlStateManager.disableAlpha();
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote && world.getWorldType() instanceof EarthWorldType && MC.isIntegratedServerRunning()) {
            awaitingLoad = true;
        }
    }

    @SubscribeEvent
    public static void onGuiChange(GuiOpenEvent event) {
        GuiScreen currentScreen = MC.currentScreen;
        if (currentScreen instanceof RemoteDataWarningGui && !((RemoteDataWarningGui) currentScreen).isComplete()) {
            event.setCanceled(true);
            ((RemoteDataWarningGui) currentScreen).setParent(event.getGui());
        }
    }

    private static void drawLoadingState(int mouseX, int mouseY) {
        if (TerrariumConfig.dataStatusIcon) {
            LoadingState state = LoadingStateHandler.checkState();

            if (state != null) {
                ScaledResolution resolution = new ScaledResolution(MC);

                MC.getTextureManager().bindTexture(WIDGETS_TEXTURE);

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                int animationTicks = ticks % 40 / 5;
                int frame = animationTicks >= 5 ? 3 - (animationTicks - 5) : animationTicks;

                drawTexturedRect(10, 10, frame * 10, state.getTextureY(), 10, 10, 256, 256);

                if (mouseX >= 10 && mouseY >= 10 && mouseX <= 20 && mouseY <= 20) {
                    String name = TextFormatting.WHITE + I18n.translateToLocal(state.getLanguageKey() + ".name");
                    String tooltip = TextFormatting.GRAY + I18n.translateToLocal(state.getLanguageKey() + ".tooltip");
                    List<String> lines = Lists.newArrayList(name, tooltip);
                    GuiUtils.drawHoveringText(lines, mouseX, mouseY, resolution.getScaledWidth(), resolution.getScaledHeight(), -1, MC.fontRenderer);

                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }

    private static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height, int mapWidth, int mapHeight) {
        float scaleX = 1.0F / mapWidth;
        float scaleY = 1.0F / mapHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.pos(x, y + height, 0.0).tex(textureX * scaleX, (textureY + height) * scaleY).endVertex();
        builder.pos(x + width, y + height, 0.0).tex((textureX + width) * scaleX, (textureY + height) * scaleY).endVertex();
        builder.pos(x + width, y, 0.0).tex((textureX + width) * scaleX, textureY * scaleY).endVertex();
        builder.pos(x, y, 0.0).tex(textureX * scaleX, textureY * scaleY).endVertex();
        tessellator.draw();
    }
}
