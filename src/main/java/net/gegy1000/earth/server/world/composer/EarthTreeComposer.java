package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.event.ConfigureTreesEvent;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.cover.CoverSelectors;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public final class EarthTreeComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final SpatialRandom random;

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();
    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthData.COVER, Cover.NO);

    private final GrowthPredictors predictors = new GrowthPredictors();

    public EarthTreeComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        ColumnDataCache dataCache = terrarium.getDataCache();
        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();

        Cover cover = this.coverSampler.sample(dataCache, dataX, dataZ);
        if (cover.is(CoverMarkers.NO_VEGETATION)) return;

        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        this.predictorSampler.sampleTo(dataCache, dataX, dataZ, this.predictors);

        TreeDecorator.Builder trees = new TreeDecorator.Builder(this.predictors);
        trees.setRadius(Trees.RADIUS);

        if (cover.is(CoverMarkers.FOREST)) {
            this.configureForestDensity(cover, trees);
        } else if (cover.is(CoverMarkers.MODERATE_TREES)) {
            trees.setDensity(0.0F, 0.1F);
        } else {
            trees.setDensity(0.0F, 0.025F);
        }

        this.addTreeCandidates(cover, trees);

        MinecraftForge.TERRAIN_GEN_BUS.post(new ConfigureTreesEvent(terrarium, cover, this.predictors, trees));

        trees.build().decorate(writer, pos, this.random);
    }

    private void configureForestDensity(Cover cover, TreeDecorator.Builder trees) {
        if (cover.is(CoverMarkers.CLOSED_TO_OPEN_FOREST)) {
            trees.setDensity(0.15F, 0.9F);
        } else if (cover.is(CoverMarkers.CLOSED_FOREST)) {
            trees.setDensity(0.5F, 0.9F);
        } else if (cover.is(CoverMarkers.OPEN_FOREST)) {
            trees.setDensity(0.15F, 0.4F);
        }
    }

    private void addTreeCandidates(Cover cover, TreeDecorator.Builder trees) {
        if (cover.is(CoverSelectors.broadleafDeciduous())) {
            trees.addCandidate(Trees.OAK);
            trees.addCandidate(Trees.ACACIA);
        }

        if (cover.is(CoverSelectors.broadleafEvergreen())) {
            trees.addCandidate(Trees.JUNGLE);
            trees.addCandidate(Trees.BIG_JUNGLE);
        }

        if (cover.is(CoverSelectors.needleleafDeciduous())) {
            trees.addCandidate(Trees.BIRCH);
            trees.addCandidate(Trees.ACACIA);
        }

        if (cover.is(CoverSelectors.needleleafEvergreen())) {
            trees.addCandidate(Trees.SPRUCE);
            trees.addCandidate(Trees.PINE);
        }
    }
}
