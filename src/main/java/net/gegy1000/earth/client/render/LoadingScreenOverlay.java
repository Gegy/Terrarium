package net.gegy1000.earth.client.render;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.earth.client.LoadingWorldGetter;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Terrarium.MODID, value = Side.CLIENT)
public class LoadingScreenOverlay {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static Field framebufferField;

    public static void onRender() {
        if (LoadingWorldGetter.getLoadingWorldType() instanceof EarthWorldType) {
            ScaledResolution resolution = new ScaledResolution(MC);

            int x = resolution.getScaledWidth() / 2;
            int y = resolution.getScaledHeight() - 40;

            String header = TextFormatting.YELLOW.toString() + TextFormatting.BOLD + I18n.format("gui.earth.credits");
            GuiRenderUtils.drawCenteredString(header, x, y, 0xFFFFFF);
            GuiRenderUtils.drawCenteredString(TextFormatting.GRAY + "NASA SRTM,", x, y + 11, 0xFFFFFF);
            GuiRenderUtils.drawCenteredString(TextFormatting.GRAY + "ESA GlobCover,", x, y + 20, 0xFFFFFF);
            GuiRenderUtils.drawCenteredString(TextFormatting.GRAY + "© OpenStreetMap Contributors", x, y + 29, 0xFFFFFF);
        }
    }

    @SubscribeEvent
    public static void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (MC.currentScreen instanceof GuiDownloadTerrain || MC.currentScreen instanceof GuiScreenWorking) {
            LoadingScreenOverlay.onRender();
        }
    }

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        try {
            LoadingScreenOverlay.hookFramebuffer();
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Failed to hook LoadingScreenRenderer framebuffer", e);
        }
    }

    public static void onPostInit() {
        try {
            LoadingScreenOverlay.framebufferField = LoadingScreenOverlay.reflectFramebufferField();
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Failed to reflect LoadingScreenRenderer framebuffer", e);
        }
    }

    private static Field reflectFramebufferField() throws Exception {
        Field framebufferField = null;
        for (Field field : LoadingScreenRenderer.class.getDeclaredFields()) {
            if (field.getType() == Framebuffer.class) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");

                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.setAccessible(true);

                framebufferField = field;
            }
        }
        if (framebufferField == null) {
            throw new ReflectiveOperationException("Could not find Framebuffer field to hook");
        }
        return framebufferField;
    }

    private static void hookFramebuffer() throws Exception {
        if (LoadingScreenOverlay.framebufferField != null) {
            LoadingScreenRenderer loadingScreen = Minecraft.getMinecraft().loadingScreen;
            Framebuffer delegate = (Framebuffer) LoadingScreenOverlay.framebufferField.get(loadingScreen);
            if (!LoadingScreenOverlay.checkHooked(delegate)) {
                LoadingScreenOverlay.framebufferField.set(loadingScreen, new HookedFramebuffer(delegate));
            }
        }
    }

    private static boolean checkHooked(Framebuffer framebuffer) {
        return framebuffer instanceof HookedFramebuffer;
    }

    private static class HookedFramebuffer extends Framebuffer {
        private final Framebuffer delegate;

        private HookedFramebuffer(Framebuffer delegate) {
            super(delegate.framebufferWidth, delegate.framebufferHeight, delegate.useDepth);
            this.delegate = delegate;
        }

        @Override
        public void createBindFramebuffer(int width, int height) {
            if (this.delegate != null) {
                this.delegate.createBindFramebuffer(width, height);
            } else {
                super.createBindFramebuffer(width, height);
            }
        }

        @Override
        public void deleteFramebuffer() {
            if (this.delegate != null) {
                this.delegate.deleteFramebuffer();
            } else {
                super.deleteFramebuffer();
            }
        }

        @Override
        public void createFramebuffer(int width, int height) {
            if (this.delegate != null) {
                this.delegate.createFramebuffer(width, height);
            } else {
                this.framebufferWidth = width;
                this.framebufferHeight = height;
                this.framebufferTextureWidth = width;
                this.framebufferTextureHeight = height;
            }
        }

        @Override
        public void setFramebufferFilter(int framebufferFilter) {
            this.delegate.setFramebufferFilter(framebufferFilter);
        }

        @Override
        public void checkFramebufferComplete() {
            if (this.delegate != null) {
                this.delegate.checkFramebufferComplete();
            }
        }

        @Override
        public void bindFramebufferTexture() {
            this.delegate.bindFramebufferTexture();
        }

        @Override
        public void unbindFramebufferTexture() {
            this.delegate.unbindFramebufferTexture();
        }

        @Override
        public void bindFramebuffer(boolean viewport) {
            this.delegate.bindFramebuffer(viewport);
        }

        @Override
        public void unbindFramebuffer() {
            LoadingScreenOverlay.onRender();
            this.delegate.unbindFramebuffer();
        }

        @Override
        public void setFramebufferColor(float red, float green, float blue, float alpha) {
            this.delegate.setFramebufferColor(red, green, blue, alpha);
        }

        @Override
        public void framebufferRender(int width, int height) {
            this.delegate.framebufferRender(width, height);
        }

        @Override
        public void framebufferRenderExt(int width, int height, boolean material) {
            this.delegate.framebufferRenderExt(width, height, material);
        }

        @Override
        public void framebufferClear() {
            this.delegate.framebufferClear();
        }

        @Override
        public boolean enableStencil() {
            return this.delegate.enableStencil();
        }

        @Override
        public boolean isStencilEnabled() {
            return this.delegate.isStencilEnabled();
        }
    }
}
