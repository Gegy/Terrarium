package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.GenerationRegion;
import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class EarthGenerationHandler {
    private final EarthGenerationSettings settings;

    private final GenerationRegionHandler regionHandler;

    private final int maxHeight;

    private final int oceanHeight;
    private final int scatterRange;

    private final Random random = new Random();

    public EarthGenerationHandler(TerrariumWorldData worldData, EarthGenerationSettings settings, int maxHeight) {
        this.settings = settings;
        this.maxHeight = maxHeight;

        this.regionHandler = new GenerationRegionHandler(worldData, this);

        this.oceanHeight = this.settings.heightOffset + 1;
        this.scatterRange = MathHelper.floor(this.settings.scatterRange * this.settings.worldScale);
    }

    public void initializeSeed(RegionTilePos pos) {
        this.random.setSeed(pos.getTileX() * 341873128712L + pos.getTileZ() * 132897987541L);
    }

    public void populateHeightRegion(int[] buffer, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        try {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockZ = globalZ + localZ;

                for (int localX = 0; localX < 16; localX++) {
                    int blockX = globalX + localX;

                    GenerationRegion region = this.regionHandler.get(blockX, blockZ);
                    int offsetHeight = region.getHeight(blockX, blockZ) + this.settings.heightOffset;
                    buffer[localX + localZ * 16] = MathHelper.clamp(offsetHeight, 0, this.maxHeight);
                }
            }
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to populate heightmap for {}, {}", chunkX, chunkZ, e);
        }
    }

    public void populateGlobRegion(GlobType[] buffer, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        try {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockZ = globalZ + localZ;
                for (int localX = 0; localX < 16; localX++) {
                    int blockX = globalX + localX;
                    buffer[localX + localZ * 16] = this.getGlobScattered(blockX, blockZ);
                }
            }
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to populate globcover region for {}, {}", chunkX, chunkZ, e);
        }
    }

    private GlobType getGlobScattered(int x, int z) {
        GlobType originGlob = this.getGlob(x, z);

        int range = Math.max(1, MathHelper.ceil(this.scatterRange * originGlob.getScatterRange()));

        int scatterX = x + this.random.nextInt(range) - this.random.nextInt(range);
        int scatterZ = z + this.random.nextInt(range) - this.random.nextInt(range);

        GlobType scattered = this.getGlob(scatterX, scatterZ);

        if (!scattered.canScatterTo()) {
            return originGlob;
        }

        return scattered;
    }

    private GlobType getGlob(int x, int z) {
        return this.regionHandler.get(x, z).getGlobType(x, z);
    }

    public EarthGenerationSettings getSettings() {
        return this.settings;
    }

    public int getOceanHeight() {
        return this.oceanHeight;
    }
}
