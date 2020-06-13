package net.gegy1000.earth.server.world.composer.decoration;

import net.gegy1000.earth.server.event.ConfigureShrubsEvent;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.Shrubs;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public final class EarthShrubComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final SpatialRandom random;

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();
    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthData.COVER, Cover.NO);

    private final GrowthPredictors predictors = new GrowthPredictors();

    public EarthShrubComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        ColumnDataCache dataCache = terrarium.getDataCache();
        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();

        Cover cover = this.coverSampler.sample(dataCache, dataX, dataZ);
        if (cover.is(CoverMarkers.NO_VEGETATION)) return;

        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        this.predictorSampler.sampleTo(dataCache, dataX, dataZ, this.predictors);

        TreeDecorator.Builder shrubs = new TreeDecorator.Builder(this.predictors);
        shrubs.setRadius(Shrubs.RADIUS);

        if (cover.is(CoverMarkers.DENSE_SHRUBS)) {
            shrubs.setDensity(0.1F, 0.25F);
        } else if (cover.is(CoverMarkers.SPARSE_SHRUBS)) {
            shrubs.setDensity(0.0F, 0.1F);
        } else {
            shrubs.setDensity(0.0F, 0.0125F);
        }

        if (cover.is(CoverMarkers.CLOSED_FOREST) || cover.is(CoverMarkers.CLOSED_TO_OPEN_FOREST)) {
            this.addFloorShrubCandidates(shrubs);
        } else {
            this.addTallShrubCandidates(shrubs);
        }

        MinecraftForge.TERRAIN_GEN_BUS.post(new ConfigureShrubsEvent(cover, this.predictors, shrubs));

        shrubs.build().decorate(writer, pos, this.random);
    }

    private void addTallShrubCandidates(TreeDecorator.Builder shrubs) {
        shrubs.addCandidate(Shrubs.TALL_OAK);
        shrubs.addCandidate(Shrubs.TALL_ACACIA);
        shrubs.addCandidate(Shrubs.TALL_JUNGLE);
        shrubs.addCandidate(Shrubs.TALL_BIRCH);
        shrubs.addCandidate(Shrubs.TALL_ACACIA);
        shrubs.addCandidate(Shrubs.TALL_SPRUCE);
    }

    private void addFloorShrubCandidates(TreeDecorator.Builder shrubs) {
        shrubs.addCandidate(Shrubs.FLOOR_OAK);
        shrubs.addCandidate(Shrubs.FLOOR_ACACIA);
        shrubs.addCandidate(Shrubs.FLOOR_JUNGLE);
        shrubs.addCandidate(Shrubs.FLOOR_BIRCH);
        shrubs.addCandidate(Shrubs.FLOOR_ACACIA);
        shrubs.addCandidate(Shrubs.FLOOR_SPRUCE);
    }
}
