package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.world.World;

public interface CoverGenerationContext {
    World getWorld();

    long getSeed();

    void prepareChunk(GenerationRegionHandler regionHandler);

    ShortRasterTile getHeightRaster();

    CoverRasterTile getCoverRaster();

    class Default implements CoverGenerationContext {
        private final World world;
        private final RegionComponentType<ShortRasterTile> heightComponent;
        private final RegionComponentType<CoverRasterTile> coverComponent;
        private ShortRasterTile heightTile;
        private CoverRasterTile coverTile;

        public Default(World world, RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<CoverRasterTile> coverComponent) {
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
        public void prepareChunk(GenerationRegionHandler regionHandler) {
            this.heightTile = regionHandler.getCachedChunkRaster(this.heightComponent);
            this.coverTile = regionHandler.getCachedChunkRaster(this.coverComponent);
        }

        @Override
        public ShortRasterTile getHeightRaster() {
            return this.heightTile;
        }

        @Override
        public CoverRasterTile getCoverRaster() {
            return this.coverTile;
        }
    }
}
