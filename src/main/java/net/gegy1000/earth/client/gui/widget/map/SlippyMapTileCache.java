package net.gegy1000.earth.client.gui.widget.map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.earth.server.world.pipeline.source.EarthRemoteData;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@SideOnly(Side.CLIENT)
public class SlippyMapTileCache {
    private static final File CACHE_ROOT = new File(TiledDataSource.GLOBAL_CACHE_ROOT, "carto");
    private static final int CACHE_SIZE = 256;

    private final ExecutorService loadingService = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("terrarium-map-load-%d")
            .build());

    private final LoadingCache<SlippyMapTilePos, SlippyMapTile> tileCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .<SlippyMapTilePos, SlippyMapTile>removalListener(notification -> {
                SlippyMapTile tile = notification.getValue();
                if (tile != null) {
                    tile.delete();
                }
            })
            .build(new CacheLoader<SlippyMapTilePos, SlippyMapTile>() {
                @Override
                public SlippyMapTile load(SlippyMapTilePos key) {
                    SlippyMapTile tile = new SlippyMapTile(key);
                    SlippyMapTileCache.this.loadingService.submit(() -> tile.supplyImage(SlippyMapTileCache.this.downloadImage(key)));
                    return tile;
                }
            });

    private final Queue<InputStream> loadingStreams = new LinkedBlockingQueue<>();

    public SlippyMapTile getTile(SlippyMapTilePos pos) {
        try {
            return this.tileCache.get(pos);
        } catch (Exception e) {
            SlippyMapTile tile = new SlippyMapTile(pos);
            tile.supplyImage(this.createErrorImage());
            return tile;
        }
    }

    public void shutdown() {
        for (SlippyMapTile tile : this.tileCache.asMap().values()) {
            tile.delete();
        }

        this.tileCache.invalidateAll();
        this.loadingService.shutdown();

        while (!this.loadingStreams.isEmpty()) {
            try {
                this.loadingStreams.poll().close();
            } catch (IOException e) {
                Terrarium.LOGGER.warn("Failed to close loading map stream", e);
            }
        }
    }

    private BufferedImage downloadImage(SlippyMapTilePos pos) {
        try (InputStream input = this.getStream(pos)) {
            return ImageIO.read(input);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load map tile {}", e.getClass().getName());
        }
        return this.createErrorImage();
    }

    private InputStream getStream(SlippyMapTilePos pos) throws IOException {
        File cacheFile = new File(CACHE_ROOT, pos.getCacheName());
        if (cacheFile.exists()) {
            return new BufferedInputStream(new FileInputStream(cacheFile));
        }
        String query = String.format(EarthRemoteData.info.getRasterMapQuery(), pos.getZoom(), pos.getX(), pos.getY());
        URL url = new URL(EarthRemoteData.info.getRasterMapEndpoint() + "/" + query);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", Terrarium.MODID);
        InputStream stream = connection.getInputStream();
        this.loadingStreams.add(stream);
        try (InputStream input = new BufferedInputStream(stream)) {
            byte[] data = IOUtils.toByteArray(input);
            this.cacheData(cacheFile, data);
            this.loadingStreams.remove(stream);
            return new ByteArrayInputStream(data);
        }
    }

    private void cacheData(File cacheFile, byte[] data) {
        if (!CACHE_ROOT.exists()) {
            CACHE_ROOT.mkdirs();
        }
        try (OutputStream output = new FileOutputStream(cacheFile)) {
            output.write(data);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to cache map raster tile", e);
        }
    }

    private BufferedImage createErrorImage() {
        BufferedImage result = new BufferedImage(SlippyMap.TILE_SIZE, SlippyMap.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        FontMetrics metrics = graphics.getFontMetrics();

        String message = "Failed to download tile";

        int x = (SlippyMap.TILE_SIZE - metrics.stringWidth(message)) / 2;
        int y = (SlippyMap.TILE_SIZE - metrics.getHeight()) / 2;
        graphics.drawString(message, x, y);

        graphics.dispose();

        return result;
    }
}
