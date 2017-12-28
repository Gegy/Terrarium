package net.gegy1000.terrarium.client.gui.widget.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;

@SideOnly(Side.CLIENT)
public class SlippyMapTile {
    private final SlippyMapTilePos pos;

    private final Object lock = new Object();

    private float transition;

    private BufferedImage image;
    private ResourceLocation location;

    public SlippyMapTile(SlippyMapTilePos pos) {
        this.pos = pos;
    }

    public SlippyMapTile(SlippyMapTilePos pos, BufferedImage image) {
        this(pos);
        this.supplyImage(image);
    }

    public void update(float partialTicks) {
        if (this.transition < 1.0F) {
            this.transition = MathHelper.clamp(this.transition + partialTicks * 0.1F, 0.0F, 1.0F);
        }
    }

    public void supplyImage(BufferedImage image) {
        synchronized (this.lock) {
            this.image = image;
        }
    }

    public ResourceLocation getLocation() {
        if (this.location == null && this.image != null) {
            this.location = this.uploadImage();
        }
        return this.location;
    }

    public float getTransition() {
        return this.transition;
    }

    public void delete() {
        Minecraft.getMinecraft().getTextureManager().deleteTexture(this.location);
    }

    private ResourceLocation uploadImage() {
        synchronized (this.lock) {
            BufferedImage image = this.image;
            this.image = null;

            DynamicTexture texture = new DynamicTexture(image);
            return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("terrarium_map_" + this.pos.toString(), texture);
        }
    }

    public boolean ready() {
        return this.getLocation() != null;
    }
}
