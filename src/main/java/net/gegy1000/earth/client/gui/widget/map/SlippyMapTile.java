package net.gegy1000.earth.client.gui.widget.map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SlippyMapTile {
    private final SlippyMapTilePos pos;

    private final Object lock = new Object();

    private float transition;

    private NativeImage image;
    private Identifier location;

    public SlippyMapTile(SlippyMapTilePos pos) {
        this.pos = pos;
    }

    public void update(float partialTicks) {
        if (this.transition < 1.0F) {
            this.transition = MathHelper.clamp(this.transition + partialTicks * 0.1F, 0.0F, 1.0F);
        }
    }

    public void supplyImage(NativeImage image) {
        synchronized (this.lock) {
            this.image = image;
        }
    }

    public Identifier getLocation() {
        if (this.location == null && this.image != null) {
            this.location = this.uploadImage();
        }
        return this.location;
    }

    public float getTransition() {
        return this.transition;
    }

    public void delete() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.location);
    }

    private Identifier uploadImage() {
        synchronized (this.lock) {
            NativeImage image = this.image;
            this.image = null;

            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            return MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("terrarium_map_" + this.pos.toString(), texture);
        }
    }

    public boolean isReady() {
        return this.getLocation() != null;
    }
}
