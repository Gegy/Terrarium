package net.gegy1000.terrarium.client.gui.widget.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class SlippyMap {
    public static final int TILE_SIZE = 256;

    public static final int MIN_ZOOM = 3;
    public static final int MAX_ZOOM = 15;

    private static final Minecraft MC = Minecraft.getMinecraft();

    private final int width;
    private final int height;
    private final Camera camera;

    private final SlippyMapTileCache cache = new SlippyMapTileCache();

    public SlippyMap(int width, int height) {
        this.width = width;
        this.height = height;

        int scale = new ScaledResolution(MC).getScaleFactor();
        this.camera = new Camera(new SlippyMapPoint(0.0, 0.0), width * scale, height * scale);
    }

    public SlippyMapTile getTile(SlippyMapTilePos pos) {
        return this.cache.getTile(pos);
    }

    public void zoom(int step, int pivotX, int pivotY) {
        int scale = new ScaledResolution(MC).getScaleFactor();
        this.camera.zoom(step, pivotX * scale, pivotY * scale);
    }

    public void drag(int deltaX, int deltaY) {
        int scale = new ScaledResolution(MC).getScaleFactor();
        this.camera.pan(deltaX * scale, deltaY * scale);
    }

    public List<SlippyMapTilePos> getVisibleTiles() {
        ScaledResolution resolution = new ScaledResolution(MC);
        int scale = resolution.getScaleFactor();

        int cameraX = this.camera.getX();
        int cameraY = this.camera.getY();
        int cameraZoom = this.camera.getZoom();

        int minX = MathHelper.floor((double) cameraX / TILE_SIZE);
        int minY = MathHelper.floor((double) cameraY / TILE_SIZE);
        int maxX = MathHelper.ceil((double) (cameraX + this.width * scale) / TILE_SIZE);
        int maxY = MathHelper.ceil((double) (cameraY + this.height * scale) / TILE_SIZE);

        List<SlippyMapTilePos> visibleTiles = new ArrayList<>();
        for (int tileY = minY; tileY < maxY; tileY++) {
            for (int tileX = minX; tileX < maxX; tileX++) {
                visibleTiles.add(new SlippyMapTilePos(tileX, tileY, cameraZoom));
            }
        }

        return visibleTiles;
    }

    public List<SlippyMapTilePos> cascadeTiles(List<SlippyMapTilePos> tiles) {
        List<SlippyMapTilePos> cascaded = new ArrayList<>(tiles.size());
        for (SlippyMapTilePos pos : tiles) {
            this.cascadeTile(cascaded, pos);
        }
        return cascaded;
    }

    private void cascadeTile(List<SlippyMapTilePos> tiles, SlippyMapTilePos pos) {
        int size = 1 << pos.getZoom();
        if (pos.getX() >= 0 && pos.getY() >= 0 && pos.getX() < size && pos.getY() < size) {
            SlippyMapTile image = this.cache.getTile(pos);
            if (image != null && image.isReady()) {
                tiles.add(pos);
            }
            if (pos.getZoom() >= MIN_ZOOM && (image == null || !image.isReady() || image.getTransition() < 1.0)) {
                this.cascadeTile(tiles, new SlippyMapTilePos(pos.getX() >> 1, pos.getY() >> 1, pos.getZoom() - 1));
            }
        }
    }

    public int getCameraX() {
        return this.camera.getX();
    }

    public int getCameraY() {
        return this.camera.getY();
    }

    public int getCameraZoom() {
        return this.camera.getZoom();
    }

    public void shutdown() {
        this.cache.shutdown();
    }

    private class Camera {
        private SlippyMapPoint origin;
        private int zoom = MIN_ZOOM;

        private Camera(SlippyMapPoint origin, int width, int height) {
            this.origin = origin.translate(-width / 2, -height / 2, this.zoom);
        }

        public void pan(int deltaX, int deltaY) {
            this.origin = this.origin.translate(deltaX, deltaY, this.zoom);
        }

        public void zoom(int steps, int pivotX, int pivotY) {
            int originX = this.origin.getX(this.zoom);
            int originY = this.origin.getY(this.zoom);

            this.zoom += steps;

            if (this.zoom < MIN_ZOOM) {
                this.zoom = MIN_ZOOM;
            } else if (this.zoom > MAX_ZOOM) {
                this.zoom = MAX_ZOOM;
            } else {
                if (steps > 0) {
                    int newX = originX * 2 + pivotX;
                    int newY = originY * 2 + pivotY;
                    this.origin = new SlippyMapPoint(newX, newY, this.zoom);
                } else if (steps < 0) {
                    int newX = (originX - pivotX) / 2;
                    int newY = (originY - pivotY) / 2;
                    this.origin = new SlippyMapPoint(newX, newY, this.zoom);
                }
            }
        }

        public SlippyMapPoint getOrigin() {
            return this.origin;
        }

        public int getX() {
            return this.origin.getX(this.zoom);
        }

        public int getY() {
            return this.origin.getY(this.zoom);
        }

        public int getZoom() {
            return this.zoom;
        }
    }
}
