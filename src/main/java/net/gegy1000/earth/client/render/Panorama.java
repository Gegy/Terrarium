package net.gegy1000.earth.client.render;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class Panorama {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final int BASE_PANORAMA_WIDTH = 416;
    private static final int TILE_SIZE = 512;

    private final String id;
    private final Identifier textureLocation;
    private final NativeImageBackedTexture texture;

    private final int stitchedWidth;
    private final int stitchedHeight;
    private final int zoom;

    private final AtomicBoolean textureDirty = new AtomicBoolean();
    private final AtomicBoolean loaded = new AtomicBoolean();

    public Panorama(String id, String suffix, int zoom) {
        this.id = id;
        this.textureLocation = new Identifier(TerrariumEarth.MODID, "panorama_" + id.hashCode() + "_" + suffix);

        this.zoom = zoom;
        this.stitchedWidth = (1 << this.zoom) * BASE_PANORAMA_WIDTH;
        this.stitchedHeight = this.stitchedWidth / 2;

        this.texture = new NativeImageBackedTexture(this.stitchedWidth, this.stitchedHeight, true);
        CLIENT.getTextureManager().registerTexture(this.textureLocation, this.texture);

        Thread thread = new Thread(this::loadTiles);
        thread.setName("Panorama Load Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void loadTiles() {
        int tileCountX = (int) Math.ceil((double) this.stitchedWidth / TILE_SIZE);
        int tileCountY = (int) Math.ceil((double) this.stitchedHeight / TILE_SIZE);
        for (int tileY = 0; tileY < tileCountY; tileY++) {
            for (int tileX = 0; tileX < tileCountX; tileX++) {
                try {
                    BufferedImage image = PanoramaLookupHandler.loadPanoramaTile(this.id, tileX, tileY, this.zoom);
                    this.stitchTile(tileX, tileY, image);
                } catch (Exception e) {
                    TerrariumEarth.LOGGER.error("Failed to load panorama tile at {} {}", tileX, tileY, e);
                }
            }
        }
        this.loaded.set(true);
    }

    private void stitchTile(int tileX, int tileY, BufferedImage image) {
        int originX = tileX * TILE_SIZE;
        int originY = tileY * TILE_SIZE;
        int maxX = Math.min(image.getWidth(), this.stitchedWidth - originX);
        int maxY = Math.min(image.getHeight(), this.stitchedHeight - originY);

        NativeImage nativeImage = this.texture.getImage();
        if (nativeImage == null) {
            throw new IllegalStateException("Panorama image has been released");
        }

        for (int localY = 0; localY < maxY; localY++) {
            for (int localX = 0; localX < maxX; localX++) {
                int globalX = originX + localX;
                int globalY = originY + localY;
                nativeImage.setPixelRGBA(globalX, globalY, image.getRGB(localX, localY));
            }
        }

        this.textureDirty.set(true);
    }

    public Identifier getTextureLocation() {
        if (this.textureDirty.get()) {
            this.textureDirty.set(false);
            this.texture.upload();
        }
        return this.textureLocation;
    }

    public void delete() {
        CLIENT.getTextureManager().destroyTexture(this.textureLocation);
    }

    public boolean hasLoaded() {
        return this.loaded.get();
    }
}
