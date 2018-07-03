package net.gegy1000.earth.client.gui.widget.map;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SlippyMapTilePos {
    private final int x;
    private final int y;
    private final int zoom;

    public SlippyMapTilePos(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZoom() {
        return this.zoom;
    }

    public String getCacheName() {
        return this.zoom + "_" + this.x + "_" + this.y + ".png";
    }

    @Override
    public int hashCode() {
        return (31 * this.x) + (31 * this.zoom) + (31 * this.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlippyMapTilePos) {
            SlippyMapTilePos tilePos = (SlippyMapTilePos) obj;
            return tilePos.x == this.x && tilePos.y == this.y && tilePos.zoom == this.zoom;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.x + "_" + this.y + "_" + this.zoom;
    }
}
