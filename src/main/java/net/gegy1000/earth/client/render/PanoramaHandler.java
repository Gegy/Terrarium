package net.gegy1000.earth.client.render;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.config.TerrariumEarthConfig;
import net.gegy1000.terrarium.server.util.FlipFlopTimer;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = TerrariumEarth.ID, value = Side.CLIENT)
public class PanoramaHandler {
    private static final String ATTRIBUTION = "\u00a9 Google Street View";

    public static final double IMMERSION_MIN_DISTANCE = 1.5 * 1.5;

    private static State state = null;

    private static double lastTicks;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        lastTicks = event.getWorld().getTotalWorldTime();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (state != null) {
                state.renderGui(Minecraft.getMinecraft(), event.getResolution(), event.getPartialTicks());
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (state != null && event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            if (player != null) {
                State newState = state.update(player.world, player);
                if (newState != state) {
                    state.delete();
                }
                state = newState;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (state != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableFog();
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            RenderHelper.enableStandardItemLighting();

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

            World world = Minecraft.getMinecraft().world;

            float partialTicks = event.getPartialTicks();
            double ticks = world.getTotalWorldTime() + partialTicks;

            state.renderWorld(Minecraft.getMinecraft(), partialTicks, (float) (ticks - lastTicks));

            lastTicks = ticks;

            GlStateManager.disableFog();
        }
    }

    public static void setState(State newState) {
        if (state != null) {
            state.delete();
        }
        state = newState;
    }

    public interface State {
        default void renderWorld(Minecraft mc, float partialTicks, float deltaTicks) {
        }

        default void renderGui(Minecraft mc, ScaledResolution resolution, float partialTicks) {
        }

        default void delete() {
        }

        @Nullable
        default State update(World world, EntityPlayer player) {
            return this;
        }
    }

    public static class Located implements State {
        private final String id;
        private final Coordinate coord;

        public Located(String id, Coordinate coord) {
            this.id = id;
            this.coord = coord;
        }

        @Override
        public void renderWorld(Minecraft mc, float partialTicks, float deltaTicks) {
            double deltaX = this.coord.getBlockX() - TileEntityRendererDispatcher.staticPlayerX;
            double deltaZ = this.coord.getBlockZ() - TileEntityRendererDispatcher.staticPlayerZ;
            double y = -TileEntityRendererDispatcher.staticPlayerY;
            long worldTime = mc.world.getTotalWorldTime();

            GlStateManager.disableFog();

            mc.getTextureManager().bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
            TileEntityBeaconRenderer.renderBeamSegment(deltaX, y, deltaZ, partialTicks, 1.0F, worldTime, 0, 256, new float[] { 0.2F, 1.0F, 0.2F });

            GlStateManager.enableFog();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Nullable
        @Override
        public State update(World world, EntityPlayer player) {
            double deltaX = player.posX - this.coord.getBlockX();
            double deltaZ = player.posZ - this.coord.getBlockZ();
            if (deltaX * deltaX + deltaZ * deltaZ < IMMERSION_MIN_DISTANCE) {
                return new Immersed(this.id, this.coord.getBlockX(), player.posY, this.coord.getBlockZ());
            }
            return this;
        }
    }

    public static class Immersed implements State {
        private static final int SPHERE_RESOLUTION = 32;
        private static final float SPHERE_RADIUS = 8.0F;

        private final String id;

        private final double originX;
        private final double originY;
        private final double originZ;

        private Panorama basePanorama;
        private Panorama panorama;

        private final Sphere sphere;
        private int sphereDisplayList = Integer.MIN_VALUE;

        private final FlipFlopTimer fadeAnimation = new FlipFlopTimer(0.1F);
        private boolean loadedBase;

        public Immersed(String id, double originX, double originY, double originZ) {
            this.id = id;
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.basePanorama = new Panorama(id, "base", 0);
            this.sphere = new Sphere();
            this.sphere.setTextureFlag(true);
            this.sphere.setOrientation(GLU.GLU_INSIDE);
            this.sphere.setNormals(GLU.GLU_NONE);
        }

        @Override
        public void renderWorld(Minecraft mc, float partialTicks, float deltaTicks) {
            GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();

            GlStateManager.pushMatrix();

            this.updateAnimation(deltaTicks);

            double renderX = this.originX - TileEntityRendererDispatcher.staticPlayerX;
            double renderY = this.originY - TileEntityRendererDispatcher.staticPlayerY;
            double renderZ = this.originZ - TileEntityRendererDispatcher.staticPlayerZ;

            GlStateManager.translate(renderX, renderY, renderZ);

            GlStateManager.scale(1.0F, 1.0F, -1.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);

            float fadeValue = this.fadeAnimation.getValue();

            float scale = (1.0F - fadeValue) * 1.4F + 1.0F;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, fadeValue);

            if (this.sphereDisplayList == Integer.MIN_VALUE) {
                this.sphereDisplayList = GLAllocation.generateDisplayLists(1);
                GlStateManager.glNewList(this.sphereDisplayList, GL11.GL_COMPILE);
                this.sphere.draw(SPHERE_RADIUS, SPHERE_RESOLUTION, SPHERE_RESOLUTION);
                GlStateManager.glEndList();
            }

            if (this.basePanorama != null) {
                mc.getTextureManager().bindTexture(this.basePanorama.getTextureLocation());
                GlStateManager.callList(this.sphereDisplayList);
            }

            if (this.panorama != null) {
                mc.getTextureManager().bindTexture(this.panorama.getTextureLocation());
                GlStateManager.callList(this.sphereDisplayList);
            }

            GlStateManager.popMatrix();

            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
        }

        @Override
        public void renderGui(Minecraft mc, ScaledResolution resolution, float partialTicks) {
            FontRenderer fontRenderer = mc.fontRenderer;
            int attributionMaxX = fontRenderer.getStringWidth(ATTRIBUTION) + 10;
            int attributionOriginY = resolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 4;
            Gui.drawRect(0, attributionOriginY, attributionMaxX, resolution.getScaledHeight(), 0xC0101010);
            fontRenderer.drawString(ATTRIBUTION, 5, attributionOriginY + fontRenderer.FONT_HEIGHT / 2 - 1, 0xFFFFFFFF);
        }

        private void updateAnimation(float deltaTicks) {
            this.fadeAnimation.update(deltaTicks);

            if (this.basePanorama != null && this.basePanorama.hasLoaded() && !this.loadedBase) {
                this.fadeAnimation.setTarget(FlipFlopTimer.Side.RIGHT);
                this.loadedBase = true;
            }

            if (this.fadeAnimation.isRight() && this.loadedBase) {
                if (this.panorama == null) {
                    this.panorama = new Panorama(this.id, "full", TerrariumEarthConfig.streetViewZoom);
                } else if (this.basePanorama != null && this.panorama.hasLoaded()) {
                    this.basePanorama.delete();
                    this.basePanorama = null;
                }
            }
        }

        @Nullable
        @Override
        public State update(World world, EntityPlayer player) {
            if (this.fadeAnimation.isRight()) {
                double maxDistance = SPHERE_RADIUS / 5.0;
                double deltaX = player.posX - this.originX;
                double deltaY = player.posY - this.originY;
                double deltaZ = player.posZ - this.originZ;
                if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > maxDistance * maxDistance) {
                    this.fadeAnimation.setTarget(FlipFlopTimer.Side.LEFT);
                }
            }

            if (this.fadeAnimation.hasMoved() && this.fadeAnimation.isLeft()) {
                return null;
            }

            return this;
        }

        @Override
        public void delete() {
            if (this.basePanorama != null) {
                this.basePanorama.delete();
            }
            if (this.panorama != null) {
                this.panorama.delete();
            }
            GlStateManager.glDeleteLists(this.sphereDisplayList, 1);
        }
    }
}
