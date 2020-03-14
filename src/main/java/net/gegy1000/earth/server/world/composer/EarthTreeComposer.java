package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.event.ConfigureTreesEvent;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.cover.CoverSelectors;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.earth.server.world.ecology.vegetation.VegetationGenerator;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public final class EarthTreeComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;
    private static final double SUITABILITY_THRESHOLD = 0.85;

    private final SpatialRandom random;

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();
    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthDataKeys.COVER, Cover.NO);

    private final GrowthPredictors predictors = new GrowthPredictors();

    public EarthTreeComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();

        Cover cover = this.coverSampler.sample(dataCache, dataX, dataZ);

        this.predictorSampler.sampleTo(dataCache, dataX, dataZ, this.predictors);

        Builder trees = new Builder(this.predictors);

        if (cover.is(CoverMarkers.FOREST)) {
            this.configureForestDensity(cover, trees);
        } else if (cover.is(CoverMarkers.MODERATE_TREES)) {
            trees.setTreeDensity(0.0F, 0.1F);
        } else {
            trees.setTreeDensity(0.0F, 0.05F);
        }

        this.addTreeCandidates(cover, trees);

        MinecraftForge.TERRAIN_GEN_BUS.post(new ConfigureTreesEvent(cover, this.predictors, trees));

        trees.build().decorate(writer, pos, this.random);
    }

    private void configureForestDensity(Cover cover, Builder trees) {
        if (cover.is(CoverMarkers.CLOSED_TO_OPEN_FOREST)) {
            trees.setTreeDensity(0.15F, 0.9F);
        } else if (cover.is(CoverMarkers.CLOSED_FOREST)) {
            trees.setTreeDensity(0.4F, 0.9F);
        } else if (cover.is(CoverMarkers.OPEN_FOREST)) {
            trees.setTreeDensity(0.15F, 0.4F);
        } else if (cover == Cover.FRESH_FLOODED_FOREST) {
            trees.setTreeDensity(0.2F, 0.6F);
        } else if (cover == Cover.SALINE_FLOODED_FOREST) {
            trees.setTreeDensity(0.2F, 0.4F);
        }
    }

    private void addTreeCandidates(Cover cover, Builder trees) {
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

    public static class Builder {
        private final GrowthPredictors predictors;

        private final WeightedPool<VegetationGenerator> trees = new WeightedPool<>();

        private VegetationGenerator mostSuitableTree;
        private double mostSuitableTreeIndicator;

        private float minTreeDensity = 0.0F;
        private float maxTreeDensity = 0.2F;

        Builder(GrowthPredictors predictors) {
            this.predictors = predictors;
        }

        public Builder addCandidate(Vegetation tree) {
            double indicator = tree.getGrowthIndicator().evaluate(this.predictors);
            if (indicator > SUITABILITY_THRESHOLD) {
                this.trees.add(tree.getGenerator(), (float) indicator);
            }

            if (indicator > this.mostSuitableTreeIndicator) {
                this.mostSuitableTree = tree.getGenerator();
                this.mostSuitableTreeIndicator = indicator;
            }

            return this;
        }

        public Builder setTreeDensity(float minDensity, float maxDensity) {
            this.minTreeDensity = minDensity;
            this.maxTreeDensity = maxDensity;
            return this;
        }

        TreeDecorator build() {
            if (this.trees.isEmpty() && this.mostSuitableTree != null) {
                this.trees.add(this.mostSuitableTree, (float) this.mostSuitableTreeIndicator);
            }

            TreeDecorator trees = new TreeDecorator(this.trees);
            trees.setDensity(this.minTreeDensity, this.maxTreeDensity);
            trees.setRadius(Trees.RADIUS);

            return trees;
        }
    }
}
