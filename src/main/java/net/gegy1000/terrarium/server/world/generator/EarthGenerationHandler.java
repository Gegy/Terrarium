package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.GenerationRegion;
import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.glob.CoverTileAccess;
import net.gegy1000.terrarium.server.map.source.height.HeightTileAccess;
import net.gegy1000.terrarium.server.map.system.component.TerrariumComponentTypes;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class EarthGenerationHandler {
    private final long worldSeed;
    private final EarthGenerationSettings settings;

    private final GenerationRegionHandler regionHandler;

    private final int maxHeight;

    private final int oceanHeight;

    private final Random random = new Random();

    public EarthGenerationHandler(long worldSeed, TerrariumWorldData worldData, EarthGenerationSettings settings, int maxHeight) {
        this.worldSeed = worldSeed;
        this.settings = settings;
        this.maxHeight = maxHeight;

        this.regionHandler = new GenerationRegionHandler(worldData, this);

        this.oceanHeight = this.settings.heightOffset + 1;
    }

    private void initializeSeed(int chunkX, int chunkZ) {
        long seed = chunkX * 132897987541L + chunkZ * 341873128712L;
        this.random.setSeed(seed ^ this.worldSeed);
    }

    public void populateHeightRegion(int[] buffer, int chunkX, int chunkZ) {
        this.initializeSeed(chunkX, chunkZ);

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        try {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockZ = globalZ + localZ;

                for (int localX = 0; localX < 16; localX++) {
                    int blockX = globalX + localX;

                    GenerationRegion region = this.regionHandler.get(blockX, blockZ);
                    HeightTileAccess heightTile = region.getData().get(TerrariumComponentTypes.HEIGHT);

                    if (heightTile != null) {
                        short height = heightTile.getShort(blockX - region.getMinX(), blockZ - region.getMinZ());
                        int offsetHeight = height + this.settings.heightOffset;
                        buffer[localX + localZ * 16] = MathHelper.clamp(offsetHeight, 0, this.maxHeight);
                    }
                }
            }
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to populate heightmap for {}, {}", chunkX, chunkZ, e);
        }
    }

    public void populateCoverRegion(CoverType[] buffer, int chunkX, int chunkZ) {
        this.initializeSeed(chunkX, chunkZ);

        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        try {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockZ = globalZ + localZ;
                for (int localX = 0; localX < 16; localX++) {
                    int blockX = globalX + localX;
                    buffer[localX + localZ * 16] = this.getCoverScattered(blockX, blockZ);
                }
            }
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to populate cover region for {}, {}", chunkX, chunkZ, e);
        }
    }

    public void populateCoverDirect(CoverType[] buffer, int globalX, int globalZ, int width, int height) {
        if (buffer.length != width * height) {
            throw new IllegalArgumentException("Given width and height that do not match given buffer size");
        }

        try {
            for (int localZ = 0; localZ < height; localZ++) {
                int blockZ = globalZ + localZ;
                for (int localX = 0; localX < width; localX++) {
                    int blockX = globalX + localX;
                    buffer[localX + localZ * width] = this.getCover(blockX, blockZ);
                }
            }
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to populate cover region at {}, {}", globalX, globalZ, e);
        }
    }

    private CoverType getCoverScattered(int x, int z) {
        CoverType originCover = this.getCover(x, z);

        int range = Math.max(1, MathHelper.ceil(this.settings.scatterRange * originCover.getScatterRange()));

        int scatterX = x + this.random.nextInt(range) - this.random.nextInt(range);
        int scatterZ = z + this.random.nextInt(range) - this.random.nextInt(range);

        CoverType scattered = this.getCover(scatterX, scatterZ);

        if (!scattered.canScatterTo()) {
            return originCover;
        }

        return scattered;
    }

    private CoverType getCover(int x, int z) {
        GenerationRegion region = this.regionHandler.get(x, z);
        CoverTileAccess coverTile = region.getData().get(TerrariumComponentTypes.COVER);

        if (coverTile != null) {
            return coverTile.get(x - region.getMinX(), z - region.getMinZ());
        }

        return CoverType.NO_DATA;
    }

    public EarthGenerationSettings getSettings() {
        return this.settings;
    }

    public int getOceanHeight() {
        return this.oceanHeight;
    }
}
