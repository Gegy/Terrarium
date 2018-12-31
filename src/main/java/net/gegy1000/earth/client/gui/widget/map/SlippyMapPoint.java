package net.gegy1000.earth.client.gui.widget.map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SlippyMapPoint {
    private final double latitude;
    private final double longitude;

    public SlippyMapPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SlippyMapPoint(double x, double y, int zoom) {
        double maximumX = SlippyMap.TILE_SIZE * (1 << zoom);
        this.longitude = x / maximumX * 360.0 - 180;

        double maximumY = SlippyMap.TILE_SIZE * (1 << zoom);
        this.latitude = Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / maximumY)));
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getX(int zoom) {
        double maximumX = SlippyMap.TILE_SIZE * (1 << zoom);
        return (this.longitude + 180) / 360 * maximumX;
    }

    public double getY(int zoom) {
        double maximumY = SlippyMap.TILE_SIZE * (1 << zoom);
        double angle = Math.toRadians(this.latitude);
        return (1.0 - Math.log(Math.tan(angle) + 1.0 / Math.cos(angle)) / Math.PI) / 2.0 * maximumY;
    }

    public SlippyMapPoint translate(double x, double y, int zoom) {
        double currentX = this.getX(zoom);
        double currentY = this.getY(zoom);
        return new SlippyMapPoint(currentX + x, currentY + y, zoom);
    }
}
