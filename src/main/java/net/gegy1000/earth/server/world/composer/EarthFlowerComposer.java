package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.event.ConfigureFlowersEvent;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.FlowerDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Flowers;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public final class EarthFlowerComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 7877688494185984193L;

    private final SpatialRandom random;

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();
    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthData.COVER, Cover.NO);

    private final GrowthPredictors predictors = new GrowthPredictors();

    public EarthFlowerComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        ColumnDataCache dataCache = terrarium.getDataCache();
        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();

        Cover cover = this.coverSampler.sample(dataCache, dataX, dataZ);
        if (cover.is(CoverMarkers.NO_VEGETATION)) {
            return;
        }

        this.predictorSampler.sampleTo(dataCache, dataX, dataZ, this.predictors);

        FlowerDecorator flowers = new FlowerDecorator();
        flowers.add(Flowers.DANDELION, 0.1F);

        if (cover.is(CoverMarkers.FOREST)) {
            flowers.setCountPerChunk(0.5F);
        } else if (cover.is(CoverMarkers.DENSE_GRASS)) {
            flowers.setCountPerChunk(0.3F);
        } else {
            flowers.setCountPerChunk(0.2F);
        }

        if (cover.is(CoverMarkers.FOREST)) {
            this.addForestFlowers(flowers);
        } else if (cover.is(CoverMarkers.PLAINS)) {
            this.addPlainsFlowers(flowers);
        }

        if (cover.is(CoverMarkers.FLOODED)) {
            flowers.add(Flowers.BLUE_ORCHID, 3.0F);
        }

        MinecraftForge.TERRAIN_GEN_BUS.post(new ConfigureFlowersEvent(terrarium, cover, this.predictors, flowers));

        flowers.decorate(writer, pos, this.random);
    }

    private void addPlainsFlowers(FlowerDecorator flowers) {
        flowers.add(Flowers.DANDELION, 1.8F);

        flowers.add(Flowers.POPPY, 1.2F);
        flowers.add(Flowers.HOUSTONIA, 1.2F);
        flowers.add(Flowers.OXEYE_DAISY, 1.2F);

        flowers.add(Flowers.ORANGE_TULIP, 0.15F);
        flowers.add(Flowers.PINK_TULIP, 0.15F);
        flowers.add(Flowers.RED_TULIP, 0.15F);
        flowers.add(Flowers.WHITE_TULIP, 0.15F);

        flowers.add(Flowers.SUNFLOWER, 0.8F);
    }

    private void addForestFlowers(FlowerDecorator flowers) {
        flowers.add(Flowers.POPPY, 2.0F);
        flowers.add(Flowers.DANDELION, 1.0F);
        flowers.add(Flowers.ALLIUM, 1.0F);
        flowers.add(Flowers.HOUSTONIA, 1.0F);
        flowers.add(Flowers.OXEYE_DAISY, 1.0F);

        flowers.add(Flowers.ORANGE_TULIP, 1.0F);
        flowers.add(Flowers.PINK_TULIP, 1.0F);
        flowers.add(Flowers.RED_TULIP, 1.0F);
        flowers.add(Flowers.WHITE_TULIP, 1.0F);

        flowers.add(Flowers.SYRINGA, 0.5F);
        flowers.add(Flowers.ROSE, 0.5F);
        flowers.add(Flowers.PAEONIA, 0.5F);
    }
}
