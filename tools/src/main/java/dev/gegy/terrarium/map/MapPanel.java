package dev.gegy.terrarium.map;

import dev.gegy.terrarium.backend.earth.EarthConstants;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.EarthTiles;
import dev.gegy.terrarium.backend.projection.cylindrical.Mercator;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MapPanel extends JPanel implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final int TILE_SIZE = 256;

    private final MapController controller;
    private MapFeature feature;
    private final EarthTiles tileLayers;
    private final Executor executor;

    private RenderedTileMap tileMap;
    private int zoomLevel;

    private final Int2ObjectMap<RenderedTileMap> cascadedTileMaps = new Int2ObjectRBTreeMap<>();

    private int lastMouseX;
    private int lastMouseY;

    public MapPanel(final MapController controller, final EarthTiles tiles, final Executor executor, final MapFeature feature) {
        this.controller = controller;
        tileLayers = tiles;
        this.executor = executor;
        this.feature = feature;

        controller.register(this);

        zoomLevel = controller.zoomLevel();
        tileMap = createTileMap(zoomLevel);

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    public static int size(final int zoomLevel) {
        return tileEdgeCount(zoomLevel) * TILE_SIZE;
    }

    private static int tileEdgeCount(final int zoomLevel) {
        return 1 << zoomLevel;
    }

    public void setFeature(final MapFeature feature) {
        this.feature = feature;
        tileMap = createTileMap(zoomLevel);
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
    public void mouseClicked(final MouseEvent e) {
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
        tileMap = createTileMap(zoomLevel);
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

    private RenderedTileMap createTileMap(final int zoomLevel) {
        final Mercator projection = createProjection(zoomLevel);
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
}
