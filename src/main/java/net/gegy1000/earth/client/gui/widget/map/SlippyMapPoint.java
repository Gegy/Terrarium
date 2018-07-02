package net.gegy1000.earth.client.gui.widget.map;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SlippyMapPoint {
    private final double latitude;
    private final double longitude;

    public SlippyMapPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SlippyMapPoint(int x, int y, int zoom) {
        double maximumX = SlippyMap.TILE_SIZE * (1 << zoom);
        this.longitude = x / maximumX * 360.0 - 180;

        double maximumY = SlippyMap.TILE_SIZE * (1 << zoom);
        this.latitude = Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / maximumY)));
    }

    public double getSpawnpointX() {
        return this.latitude;
    }

    public double getSpawnpointZ() {
        return this.longitude;
    }

    public int getX(int zoom) {
        double maximumX = SlippyMap.TILE_SIZE * (1 << zoom);
        return MathHelper.floor((this.longitude + 180) / 360 * maximumX);
    }

    public int getY(int zoom) {
        double maximumY = SlippyMap.TILE_SIZE * (1 << zoom);
        double angle = Math.toRadians(this.latitude);
        return MathHelper.floor((1.0 - Math.log(Math.tan(angle) + 1.0 / Math.cos(angle)) / Math.PI) / 2.0 * maximumY);
    }

    public SlippyMapPoint translate(int x, int y, int zoom) {
        int currentX = this.getX(zoom);
        int currentY = this.getY(zoom);
        return new SlippyMapPoint(currentX + x, currentY + y, zoom);
    }
}
