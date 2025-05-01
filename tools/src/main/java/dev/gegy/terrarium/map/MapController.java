package dev.gegy.terrarium.map;

import dev.gegy.terrarium.backend.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MapController {
    private static final int MIN_ZOOM = 3;
    private static final int MAX_ZOOM = 15;

    private final List<MapPanel> maps = new ArrayList<>();

    private int panX;
    private int panY;
    private int zoomLevel = MIN_ZOOM;

    public void register(final MapPanel map) {
        maps.add(map);
    }

    public void pan(final int deltaX, final int deltaY) {
        setPan(panX - deltaX, panY - deltaY);
        for (final MapPanel map : maps) {
            map.mapMoved();
        }
    }

    public void zoom(final int pivotX, final int pivotY, final int amount) {
        final int newZoomLevel = Util.clamp(zoomLevel + amount, MIN_ZOOM, MAX_ZOOM);
        if (zoomLevel == newZoomLevel) {
            return;
        }
        zoomLevel = newZoomLevel;
        if (amount > 0) {
            setPan((panX << amount) + pivotX, (panY << amount) + pivotY);
        } else if (amount < 0) {
            setPan(((panX - pivotX) >> -amount), ((panY - pivotY) >> -amount));
        }
        for (final MapPanel map : maps) {
            map.mapZoomed();
        }
    }

    private void setPan(final int panX, final int panY) {
        final int size = MapPanel.size(zoomLevel);
        this.panX = panX;
        this.panY = Util.clamp(panY, 0, size - getHeight());
    }

    private int getHeight() {
        return maps.get(0).getHeight();
    }

    public int panX() {
        return panX;
    }

    public int panY() {
        return panY;
    }

    public int zoomLevel() {
        return zoomLevel;
    }
}
