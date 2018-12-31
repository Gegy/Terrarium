package net.gegy1000.earth.client.render;

// TODO: Panorama
/*@Environment(EnvType.CLIENT)
@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID, value = EnvType.CLIENT)
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
                state.renderGui(MinecraftClient.getInstance(), event.getResolution(), event.getPartialTicks());
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (state != null && event.phase == TickEvent.Phase.START) {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
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
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableFog();
            GlStateManager.enableCull();
            GlStateManager.enableTexture();
            GuiLighting.enableForItems();

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

            World world = MinecraftClient.getInstance().world;

            float partialTicks = event.getPartialTicks();
            double ticks = world.getTime() + partialTicks;

            state.renderWorld(MinecraftClient.getInstance(), partialTicks, (float) (ticks - lastTicks));

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
        default void renderWorld(MinecraftClient client, float partialTicks, float deltaTicks) {
        }

        default void renderGui(MinecraftClient client, float partialTicks) {
        }

        default void delete() {
        }

        @Nullable
        default State update(World world, PlayerEntity player) {
            return this;
        }
    }

    public static class Located implements State {
        private final String id;
        private final double latitude;
        private final double longitude;

        public Located(String id, double latitude, double longitude) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public void renderWorld(MinecraftClient client, float partialTicks, float deltaTicks) {
            EarthGeneratorConfig config = EarthGeneratorConfig.get(client.world);
            if (config != null) {
                double blockX = config.getX(this.latitude, this.longitude);
                double blockZ = config.getZ(this.latitude, this.longitude);

                double deltaX = blockX - BlockEntityRenderDispatcher.renderOffsetX;
                double deltaZ = blockZ - BlockEntityRenderDispatcher.renderOffsetZ;
                double y = -BlockEntityRenderDispatcher.renderOffsetY;
                long worldTime = client.world.getTime();

                GlStateManager.disableFog();

                client.getTextureManager().bindTexture(BeaconBlockEntityRenderer.TEXTURE_BEACON_BEAM);
                BeaconBlockEntityRenderer.method_3545(deltaX, y, deltaZ, partialTicks, 1.0F, worldTime, 0, 256, new float[] { 0.2F, 1.0F, 0.2F });

                GlStateManager.enableFog();
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        @Nullable
        @Override
        public State update(World world, PlayerEntity player) {
            EarthGeneratorConfig config = EarthGeneratorConfig.get(world);
            if (config != null) {
                double blockX = config.getX(this.latitude, this.longitude);
                double blockZ = config.getZ(this.latitude, this.longitude);
                double deltaX = player.x - blockX;
                double deltaZ = player.z - blockZ;
                if (deltaX * deltaX + deltaZ * deltaZ < IMMERSION_MIN_DISTANCE) {
                    return new Immersed(this.id, blockX, player.y, blockZ);
                }
                return this;
            }
            return null;
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
        public void renderWorld(MinecraftClient client, float partialTicks, float deltaTicks) {
            GlStateManager.cullFace(GlStateManager.FaceSides.FRONT);
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.enableBlend();

            GlStateManager.pushMatrix();

            this.updateAnimation(deltaTicks);

            double renderX = this.originX - BlockEntityRenderDispatcher.renderOffsetX;
            double renderY = this.originY - BlockEntityRenderDispatcher.renderOffsetY;
            double renderZ = this.originZ - BlockEntityRenderDispatcher.renderOffsetZ;

            GlStateManager.translated(renderX, renderY, renderZ);

            GlStateManager.scalef(1.0F, 1.0F, -1.0F);
            GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);

            float fadeValue = this.fadeAnimation.getValue();

            float scale = (1.0F - fadeValue) * 1.4F + 1.0F;
            GlStateManager.scalef(scale, scale, scale);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, fadeValue);

            if (this.sphereDisplayList == Integer.MIN_VALUE) {
                this.sphereDisplayList = GlAllocationUtils.genLists(1);
                GlStateManager.newList(this.sphereDisplayList, GL11.GL_COMPILE);
                this.sphere.draw(SPHERE_RADIUS, SPHERE_RESOLUTION, SPHERE_RESOLUTION);
                GlStateManager.endList();
            }

            if (this.basePanorama != null) {
                client.getTextureManager().bindTexture(this.basePanorama.getTextureLocation());
                GlStateManager.callList(this.sphereDisplayList);
            }

            if (this.panorama != null) {
                client.getTextureManager().bindTexture(this.panorama.getTextureLocation());
                GlStateManager.callList(this.sphereDisplayList);
            }

            GlStateManager.popMatrix();

            GlStateManager.cullFace(GlStateManager.FaceSides.BACK);
            GlStateManager.disableBlend();
            GlStateManager.enableDepthTest();
            GlStateManager.enableLighting();
        }

        @Override
        public void renderGui(MinecraftClient client, float partialTicks) {
            FontRenderer fontRenderer = client.fontRenderer;
            int attributionMaxX = fontRenderer.getStringWidth(ATTRIBUTION) + 10;
            int attributionOriginY = client.window.getScaledHeight() - fontRenderer.fontHeight - 4;
            Gui.drawRect(0, attributionOriginY, attributionMaxX, client.window.getScaledHeight(), 0xC0101010);
            fontRenderer.draw(ATTRIBUTION, 5, attributionOriginY + fontRenderer.fontHeight / 2.0F - 1, 0xFFFFFFFF);
        }

        private void updateAnimation(float deltaTicks) {
            this.fadeAnimation.update(deltaTicks);

            if (this.basePanorama != null && this.basePanorama.hasLoaded() && !this.loadedBase) {
                this.fadeAnimation.setTarget(FlipFlopTimer.Side.RIGHT);
                this.loadedBase = true;
            }

            if (this.fadeAnimation.isRight() && this.loadedBase) {
                if (this.panorama == null) {
                    this.panorama = new Panorama(this.id, "full", 2);
                } else if (this.basePanorama != null && this.panorama.hasLoaded()) {
                    this.basePanorama.delete();
                    this.basePanorama = null;
                }
            }
        }

        @Nullable
        @Override
        public State update(World world, PlayerEntity player) {
            if (this.fadeAnimation.isRight()) {
                double maxDistance = SPHERE_RADIUS / 5.0;
                double deltaX = player.x - this.originX;
                double deltaY = player.y - this.originY;
                double deltaZ = player.z - this.originZ;
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
            GlAllocationUtils.deleteSingletonList(this.sphereDisplayList);
        }
    }
}*/
