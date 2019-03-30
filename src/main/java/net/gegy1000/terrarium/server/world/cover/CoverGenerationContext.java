package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;

public interface CoverGenerationContext {
    World getWorld();

    long getSeed();

    void prepareChunk(RegionGenerationHandler regionHandler);

    ShortRaster getHeightRaster();

    CoverRaster getCoverRaster();

    class Default implements CoverGenerationContext {
        private final World world;
        private final RegionComponentType<ShortRaster> heightComponent;
        private final RegionComponentType<CoverRaster> coverComponent;
        private ShortRaster heightTile;
        private CoverRaster coverTile;

        public Default(World world, RegionComponentType<ShortRaster> heightComponent, RegionComponentType<CoverRaster> coverComponent) {
            this.world = world;
            this.heightComponent = heightComponent;
            this.coverComponent = coverComponent;
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public long getSeed() {
            return this.world.getWorldInfo().getSeed();
        }

        @Override
        public void prepareChunk(RegionGenerationHandler regionHandler) {
            this.heightTile = regionHandler.getCachedChunkRaster(this.heightComponent);
            this.coverTile = regionHandler.getCachedChunkRaster(this.coverComponent);
        }

        @Override
        public ShortRaster getHeightRaster() {
            return this.heightTile;
        }

        @Override
        public CoverRaster getCoverRaster() {
            return this.coverTile;
        }
    }
}
