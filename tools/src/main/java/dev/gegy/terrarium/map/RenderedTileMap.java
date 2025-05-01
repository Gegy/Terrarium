package dev.gegy.terrarium.map;

import dev.gegy.terrarium.backend.util.Util;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RenderedTileMap {
    private final int tileSize;
    private final Renderer renderer;

    private Tile[] tiles = new Tile[0];
    private Frame frame = Frame.EMPTY;

    private Runnable repaintListener = () -> {
    };

    public RenderedTileMap(final int tileSize, final Renderer renderer) {
        this.tileSize = tileSize;
        this.renderer = renderer;
    }

    public void setRepaintListener(final Runnable repaintListener) {
        this.repaintListener = repaintListener;
    }

    public Frame frame() {
        return frame;
    }

    @Nullable
    public BufferedImage get(final int x, final int y) {
        if (!frame.contains(x, y)) {
            return null;
        }
        return tiles[frame.index(x, y)].getImage();
    }

    public void move(final int x, final int y) {
        final Frame oldFrame = frame;
        frame = new Frame(
                Math.floorDiv(x, tileSize), Math.floorDiv(y, tileSize),
                frame.width, frame.height
        );

        for (int tileY = frame.minY(); tileY <= frame.maxY(); tileY++) {
            for (int tileX = frame.minX(); tileX <= frame.maxX(); tileX++) {
                final int index = frame.index(tileX, tileY);
                if (!oldFrame.contains(tileX, tileY)) {
                    tiles[index] = loadTile(tileX, tileY);
                }
            }
        }
    }

    public void resize(final int x, final int y, final int width, final int height) {
        final Frame oldFrame = frame;
        final Tile[] oldTiles = tiles;

        frame = new Frame(
                Math.floorDiv(x, tileSize), Math.floorDiv(y, tileSize),
                Util.ceilDiv(width, tileSize) + 1, Util.ceilDiv(height, tileSize) + 1
        );
        tiles = new Tile[frame.width * frame.height];

        for (int tileY = frame.minY(); tileY <= frame.maxY(); tileY++) {
            for (int tileX = frame.minX(); tileX <= frame.maxX(); tileX++) {
                final int index = frame.index(tileX, tileY);
                if (oldFrame.contains(tileX, tileY)) {
                    tiles[index] = oldTiles[oldFrame.index(tileX, tileY)];
                } else {
                    tiles[index] = loadTile(tileX, tileY);
                }
            }
        }
    }

    private Tile loadTile(final int x, final int y) {
        final CompletableFuture<Optional<BufferedImage>> future = renderer.render(x, y);
        future.thenRun(repaintListener);
        return new Tile(future);
    }

    public boolean isReady() {
        for (final Tile tile : tiles) {
            if (!tile.image.isDone()) {
                return false;
            }
        }
        return true;
    }

    public record Frame(int minX, int minY, int width, int height) {
        public static final Frame EMPTY = new Frame(0, 0, 0, 0);

        public int maxX() {
            return minX + width - 1;
        }

        public int maxY() {
            return minY + height - 1;
        }

        public boolean contains(final int x, final int y) {
            final int relativeX = x - minX;
            final int relativeY = y - minY;
            return relativeX >= 0 && relativeY >= 0 && relativeX < width && relativeY < height;
        }

        private int index(final int x, final int y) {
            final int indexX = Math.floorMod(x, width);
            final int indexY = Math.floorMod(y, height);
            return indexX + indexY * width;
        }
    }

    private record Tile(CompletableFuture<Optional<BufferedImage>> image) {
        @Nullable
        public BufferedImage getImage() {
            return image.getNow(Optional.empty()).orElse(null);
        }
    }

    @FunctionalInterface
    public interface Renderer {
        CompletableFuture<Optional<BufferedImage>> render(int tileX, int tileY);
    }
}
