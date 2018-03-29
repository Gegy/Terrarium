package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverPrimer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class VegetationWithCroplandCover extends EarthCoverType {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;

    @Override
    public EarthSurfaceGenerator createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this);
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        // TODO
        return CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR.apply(LatitudinalZone.TROPICS);
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;
        private final GenLayer grassSelector;

        private Surface(EarthCoverContext context, CoverType coverType) {
            super(context, coverType);

            GenLayer cover = new SelectionSeedLayer(2, 1);
            cover = new GenLayerVoronoiZoom(1000, cover);
            cover = new GenLayerFuzzyZoom(2000, cover);

            this.coverSelector = cover;
            this.coverSelector.initWorldGenSeed(context.getSeed());

            GenLayer grass = new SelectionSeedLayer(2, 3000);
            grass = new GenLayerFuzzyZoom(1000, grass);
            grass = new ReplaceRandomLayer(0, 1, 5, 2000, grass);
            grass = new ReplaceRandomLayer(1, 0, 5, 3000, grass);
            grass = new GenLayerFuzzyZoom(4000, grass);

            this.grassSelector = grass;
            this.grassSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, (sampledValue, localX, localZ) -> {
                switch (sampledValue) {
                    case LAYER_GRASS:
                        return GRASS;
                    case LAYER_DIRT:
                        return COARSE_DIRT;
                    default:
                        return GRASS;
                }
            });
        }

        @Override
        public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
            ShortRasterTileAccess heightRaster = this.context.getHeightRaster();
            int[] grassLayer = this.sampleChunk(this.grassSelector, originX, originZ);

            this.iterateChunk((localX, localZ) -> {
                int y = heightRaster.getShort(localX, localZ);
                if (grassLayer[localX + localZ * 16] == 1 && random.nextInt(4) != 0) {
                    if (random.nextInt(6) == 0) {
                        primer.setBlockState(localX, y + 1, localZ, DOUBLE_TALL_GRASS);
                        primer.setBlockState(localX, y + 2, localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                    } else {
                        primer.setBlockState(localX, y + 1, localZ, TALL_GRASS);
                    }
                }
            });
        }
    }

    private static class Decoration extends EarthDecorationGenerator {
        private Decoration(EarthCoverContext context, CoverType coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
            World world = this.context.getWorld();

            this.preventIntersection(2);

            this.decorateScatter(random, originX, originZ, this.range(random, 1, 3), (pos, localX, localZ) -> OAK_TALL_SHRUB.generate(world, random, pos));

            this.decorateScatter(random, originX, originZ, this.range(random, 5, 8), (pos, localX, localZ) -> OAK_SMALL_SHRUB.generate(world, random, pos));

            this.stopIntersectionPrevention();
        }
    }
}
