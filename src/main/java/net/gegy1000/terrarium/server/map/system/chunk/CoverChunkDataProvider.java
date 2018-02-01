package net.gegy1000.terrarium.server.map.system.chunk;

import net.gegy1000.terrarium.server.map.GenerationRegion;
import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.raster.RasterDataAccess;
import net.gegy1000.terrarium.server.map.system.component.RegionComponentType;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.gegy1000.terrarium.server.world.generator.PseudoRandomMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class CoverChunkDataProvider implements ChunkDataProvider<CoverChunkDataProvider.Data> {
    private static final long SCATTER_SEED = 5654549466233716589L;

    private final EarthGenerationSettings settings;

    private final PseudoRandomMap scatterMap;
    private final RegionComponentType<? extends RasterDataAccess<CoverType>> coverComponent;

    private final Data coverData = new Data();

    public CoverChunkDataProvider(EarthGenerationSettings settings, World world, RegionComponentType<? extends RasterDataAccess<CoverType>> coverComponent) {
        this.settings = settings;

        this.scatterMap = new PseudoRandomMap(world.getSeed(), SCATTER_SEED);
        this.coverComponent = coverComponent;
    }

    @Override
    public void populate(GenerationRegionHandler regionHandler, World world, int originX, int originZ) {
        Data coverData = this.coverData;
        coverData.reset();

        for (int localZ = 0; localZ < 16; localZ++) {
            int globalZ = originZ + localZ;
            for (int localX = 0; localX < 16; localX++) {
                int globalX = originX + localX;
                coverData.set(localX, localZ, this.getCoverScattered(regionHandler, globalX, globalZ));
            }
        }
    }

    @Override
    public Data getResultStore() {
        return this.coverData;
    }

    private CoverType getCoverScattered(GenerationRegionHandler regionHandler, int x, int z) {
        this.scatterMap.initPosSeed(x, z);

        CoverType originCover = this.getCover(regionHandler, x, z);

        int range = Math.max(1, MathHelper.ceil(this.settings.scatterRange * originCover.getScatterRange()));

        int scatterX = x + this.scatterMap.nextInt(range) - this.scatterMap.nextInt(range);
        int scatterZ = z + this.scatterMap.nextInt(range) - this.scatterMap.nextInt(range);

        CoverType scattered = this.getCover(regionHandler, scatterX, scatterZ);

        if (!scattered.canScatterTo()) {
            return originCover;
        }

        return scattered;
    }

    private CoverType getCover(GenerationRegionHandler regionHandler, int x, int z) {
        GenerationRegion region = regionHandler.get(x, z);
        RasterDataAccess<CoverType> coverTile = region.getData().get(this.coverComponent);

        if (coverTile != null) {
            return coverTile.get(x - region.getMinX(), z - region.getMinZ());
        }

        return CoverType.NO_DATA;
    }

    public static class Data {
        private final CoverType[] coverData = ArrayUtils.defaulted(new CoverType[256], CoverType.NO_DATA);
        private final Set<CoverType> types = new HashSet<>();

        public void reset() {
            this.types.clear();
        }

        public void set(int x, int y, CoverType type) {
            this.coverData[x + y * 16] = type;
            this.types.add(type);
        }

        public CoverType[] getCoverData() {
            return this.coverData;
        }

        public Set<CoverType> getTypes() {
            return this.types;
        }
    }
}
