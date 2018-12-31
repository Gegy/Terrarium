package net.gegy1000.earth.client.gui.widget.map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SlippyMap {
    public static final int TILE_SIZE = 256;

    public static final int MIN_ZOOM = 3;
    public static final int MAX_ZOOM = 15;

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private final int width;
    private final int height;
    private final Camera camera;

    private final SlippyMapTileCache cache = new SlippyMapTileCache();

    public SlippyMap(int width, int height) {
        this.width = width;
        this.height = height;

        double scale = CLIENT.window.method_4495();
        this.camera = new Camera(new SlippyMapPoint(0.0, 0.0), width * scale, height * scale);
    }

    public SlippyMapTile getTile(SlippyMapTilePos pos) {
        return this.cache.getTile(pos);
    }

    public void focus(double latitude, double longitude, int zoom) {
        double scale = CLIENT.window.method_4495();
        SlippyMapPoint point = new SlippyMapPoint(latitude, longitude);
        this.camera.focus(point.getX(zoom), point.getY(zoom), zoom, this.width * scale, this.height * scale);
    }

    public void zoom(int step, double pivotX, double pivotY) {
        double scale = CLIENT.window.method_4495();
        this.camera.zoom(step, pivotX * scale, pivotY * scale);
    }

    public void drag(double deltaX, double deltaY) {
        double scale = CLIENT.window.method_4495();
        this.camera.pan(deltaX * scale, deltaY * scale);
    }

    public Collection<SlippyMapTilePos> getVisibleTiles() {
        double scale = CLIENT.window.method_4495();

        double cameraX = this.camera.getX();
        double cameraY = this.camera.getY();
        int cameraZoom = this.camera.getZoom();

        int minX = MathHelper.floor(cameraX / TILE_SIZE);
        int minY = MathHelper.floor(cameraY / TILE_SIZE);
        int maxX = MathHelper.ceil((cameraX + this.width * scale) / TILE_SIZE);
        int maxY = MathHelper.ceil((cameraY + this.height * scale) / TILE_SIZE);

        List<SlippyMapTilePos> visibleTiles = new ArrayList<>();
        for (int tileY = minY; tileY < maxY; tileY++) {
            for (int tileX = minX; tileX < maxX; tileX++) {
                visibleTiles.add(new SlippyMapTilePos(tileX, tileY, cameraZoom));
            }
        }

        return visibleTiles;
    }

    public List<SlippyMapTilePos> cascadeTiles(Collection<SlippyMapTilePos> tiles) {
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

    public double getCameraX() {
        return this.camera.getX();
    }

    public double getCameraY() {
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

        private Camera(SlippyMapPoint origin, double width, double height) {
            this.origin = origin.translate(-width / 2, -height / 2, this.zoom);
        }

        public void focus(double x, double y, int zoom, double width, double height) {
            this.origin = new SlippyMapPoint(x, y, zoom).translate(-width / 2, -height / 2, zoom);
            this.zoom = zoom;
        }

        public void pan(double deltaX, double deltaY) {
            this.origin = this.origin.translate(deltaX, deltaY, this.zoom);
        }

        public void zoom(int steps, double pivotX, double pivotY) {
            double originX = this.origin.getX(this.zoom);
            double originY = this.origin.getY(this.zoom);

            this.zoom += steps;

            if (this.zoom < MIN_ZOOM) {
                this.zoom = MIN_ZOOM;
            } else if (this.zoom > MAX_ZOOM) {
                this.zoom = MAX_ZOOM;
            } else {
                if (steps > 0) {
                    double newX = originX * 2 + pivotX;
                    double newY = originY * 2 + pivotY;
                    this.origin = new SlippyMapPoint(newX, newY, this.zoom);
                } else if (steps < 0) {
                    double newX = (originX - pivotX) / 2;
                    double newY = (originY - pivotY) / 2;
                    this.origin = new SlippyMapPoint(newX, newY, this.zoom);
                }
            }
        }

        public double getX() {
            return this.origin.getX(this.zoom);
        }

        public double getY() {
            return this.origin.getY(this.zoom);
        }

        public int getZoom() {
            return this.zoom;
        }
    }
}
