package dev.gegy.terrarium.map;

import dev.gegy.terrarium.backend.earth.EarthConstants;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.EarthTiles;
import dev.gegy.terrarium.backend.earth.GeoCoords;
import dev.gegy.terrarium.backend.projection.cylindrical.CylindricalProjection;
import dev.gegy.terrarium.backend.projection.cylindrical.Mercator;
import dev.gegy.terrarium.backend.util.Util;
import dev.gegy.terrarium.map.feature.MapFeature;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MapPanel extends JPanel implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final int TILE_SIZE = 256;

    private final MapController controller;
    private MapFeature feature;
    private final EarthTiles tileLayers;
    private final Executor executor;

    private CylindricalProjection projection;
    private RenderedTileMap tileMap;
    private int zoomLevel;

    private final Int2ObjectMap<RenderedTileMap> cascadedTileMaps = new Int2ObjectRBTreeMap<>();

    private int lastMouseX;
    private int lastMouseY;

    private MouseClickListener mouseClickListener = (event, latitude, longitude) -> {
    };

    private final List<MapDecoration> decorations = new ArrayList<>();

    public MapPanel(final MapController controller, final EarthTiles tiles, final Executor executor, final MapFeature feature) {
        this.controller = controller;
        tileLayers = tiles;
        this.executor = executor;
        this.feature = feature;

        controller.register(this);

        zoomLevel = controller.zoomLevel();
        projection = createProjection(zoomLevel);
        tileMap = createTileMap(zoomLevel, projection);

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    public void setMouseClickListener(final MouseClickListener mouseClickListener) {
        this.mouseClickListener = mouseClickListener;
    }

    public void addDecoration(final MapDecoration decoration) {
        decorations.add(decoration);
        repaint();
    }

    public static int size(final int zoomLevel) {
        return tileEdgeCount(zoomLevel) * TILE_SIZE;
    }

    private static int tileEdgeCount(final int zoomLevel) {
        return 1 << zoomLevel;
    }

    public void setFeature(final MapFeature feature) {
        this.feature = feature;
        tileMap = createTileMap(zoomLevel, projection);
        cascadedTileMaps.clear();
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        tileMap.resize(controller.panX(), controller.panY(), getWidth(), getHeight());
    }

    @Override
    public void componentMoved(final ComponentEvent e) {
    }

    @Override
    public void componentShown(final ComponentEvent e) {
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        final Iterator<MapDecoration> iterator = decorations.iterator();
        while (iterator.hasNext()) {
            final MapDecoration decoration = iterator.next();
            final ScreenPos screenPos = getScreenPos(decoration.latitude(), decoration.longitude());
            if (!isOnScreen(screenPos)) {
                continue;
            }
            final int deltaX = event.getX() - screenPos.x();
            final int deltaY = event.getY() - screenPos.y();
            final int distanceSq = deltaX * deltaX + deltaY * deltaY;
            final int pickDistanceSq = decoration.pickRadius() * decoration.pickRadius();
            if (distanceSq <= pickDistanceSq) {
                if (decoration.clicked(event) == MapDecoration.ClickResponse.REMOVE) {
                    iterator.remove();
                    repaint();
                }
                return;
            }
        }

        final GeoCoords coords = getGeoCoordsAt(event.getX(), event.getY());
        mouseClickListener.onMouseClicked(event, coords.lat(), coords.lon());
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        final int deltaX = e.getX() - lastMouseX;
        final int deltaY = e.getY() - lastMouseY;
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        controller.pan(deltaX, deltaY);
    }

    public void mapMoved() {
        tileMap.move(controller.panX(), controller.panY());
        repaint();
    }

    public void mapZoomed() {
        cascadedTileMaps.put(zoomLevel, tileMap);
        zoomLevel = controller.zoomLevel();
        projection = createProjection(zoomLevel);
        tileMap = createTileMap(zoomLevel, projection);
        repaint();
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        final int amount = e.getUnitsToScroll() < 0 ? 1 : -1;
        controller.zoom(e.getX(), e.getY(), amount);
    }

    private GeoCoords getGeoCoordsAt(final int x, final int y) {
        final int size = size(controller.zoomLevel());
        final float latitude = (float) projection.lat(y + controller.panY() - size / 2.0);
        final float longitude = (float) projection.lon(Math.floorMod(x + controller.panX(), size) - size / 2.0);
        return new GeoCoords(latitude, longitude);
    }

    private ScreenPos getScreenPos(final double latitude, final double longitude) {
        final int size = size(controller.zoomLevel());
        final int x = Math.floorMod(Util.floorInt(projection.blockX(longitude) - controller.panX() + size / 2.0), size);
        final int y = Util.floorInt(projection.blockZ(latitude) - controller.panY() + size / 2.0);
        return new ScreenPos(x, y);
    }

    private RenderedTileMap createTileMap(final int zoomLevel, final CylindricalProjection projection) {
        final EarthLayers layers = EarthLayers.create(tileLayers, projection, executor);

        final int tileCount = tileEdgeCount(zoomLevel);
        final int halfTileCount = tileCount / 2;

        final RenderedTileMap tileMap = new RenderedTileMap(TILE_SIZE, (tileX, tileY) -> {
            if (tileY < 0 || tileY >= tileCount) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            final int wrappedTileX = Math.floorMod(tileX, tileCount);
            final int x0 = (wrappedTileX - halfTileCount) * TILE_SIZE;
            final int y0 = (tileY - halfTileCount) * TILE_SIZE;
            return feature.render(layers, wrappedTileX, tileY, zoomLevel, x0, y0, x0 + TILE_SIZE - 1, y0 + TILE_SIZE - 1);
        });
        tileMap.setRepaintListener(this::repaint);
        tileMap.resize(controller.panX(), controller.panY(), getWidth(), getHeight());

        return tileMap;
    }

    private static Mercator createProjection(final int zoomLevel) {
        // Arbitrary padding to ensure that we don't need to sample outside the source data when scaling
        final int width = size(zoomLevel) + 4;
        final double metersPerPixel = (double) EarthConstants.CIRCUMFERENCE_EQUATOR / width;
        return new Mercator(metersPerPixel);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D graphics = (Graphics2D) g;

        if (tileMap.isReady()) {
            cascadedTileMaps.clear();
        } else {
            // Pessimistically draw all tiles from former zoom levels - but they should get cleared out soon enough
            for (final Int2ObjectMap.Entry<RenderedTileMap> entry : cascadedTileMaps.int2ObjectEntrySet()) {
                final int zoomLevel = entry.getIntKey();
                final RenderedTileMap tileMap = entry.getValue();
                drawTileMap(graphics, tileMap, controller.zoomLevel() - zoomLevel);
            }
        }

        drawTileMap(graphics, tileMap, 0);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (final MapDecoration decoration : decorations) {
            final ScreenPos pos = getScreenPos(decoration.latitude(), decoration.longitude());
            if (isOnScreen(pos)) {
                decoration.draw(graphics, pos.x, pos.y);
            }
        }
    }

    private void drawTileMap(final Graphics2D graphics, final RenderedTileMap tileMap, final int relativeZoom) {
        final int drawSize = scaleByZoom(TILE_SIZE, relativeZoom);
        final RenderedTileMap.Frame frame = tileMap.frame();
        for (int tileY = frame.minY(); tileY <= frame.maxY(); tileY++) {
            for (int tileX = frame.minX(); tileX <= frame.maxX(); tileX++) {
                final BufferedImage image = tileMap.get(tileX, tileY);
                if (image == null) {
                    continue;
                }
                final int x = scaleByZoom(tileX * TILE_SIZE, relativeZoom) - controller.panX();
                final int y = scaleByZoom(tileY * TILE_SIZE, relativeZoom) - controller.panY();
                if (x < -drawSize || y < -drawSize || x >= getWidth() || y >= getHeight()) {
                    continue;
                }
                graphics.drawImage(image, x, y, drawSize, drawSize, null);
            }
        }
    }

    private static int scaleByZoom(final int coordinate, final int relativeZoom) {
        if (relativeZoom > 0) {
            return coordinate << relativeZoom;
        } else {
            return coordinate >> -relativeZoom;
        }
    }

    private boolean isOnScreen(final ScreenPos pos) {
        return pos.x >= 0 && pos.y >= 0 && pos.x < getWidth() && pos.y < getHeight();
    }

    public interface MouseClickListener {
        void onMouseClicked(MouseEvent event, double latitude, double longitude);
    }

    private record ScreenPos(int x, int y) {
    }
}
