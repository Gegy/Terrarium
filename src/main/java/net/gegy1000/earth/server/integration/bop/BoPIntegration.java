package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.biome.BOPBiomes;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.event.ClassifyBiomeEvent;
import net.gegy1000.earth.server.event.ConfigureFlowersEvent;
import net.gegy1000.earth.server.event.ConfigureTreesEvent;
import net.gegy1000.earth.server.world.Climate;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.earth.server.world.composer.decoration.OreDecorationComposer;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.cover.CoverSelectors;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSelector;
import net.gegy1000.earth.server.world.ecology.vegetation.FlowerDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.event.TerrariumInitializeGeneratorEvent;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class BoPIntegration {
    public static void setup() {
        MinecraftForge.TERRAIN_GEN_BUS.register(BoPIntegration.class);
        MinecraftForge.EVENT_BUS.register(BoPIntegration.class);
    }

    @SubscribeEvent
    public static void onConfigureTrees(ConfigureTreesEvent event) {
        TerrariumWorld terrarium = event.getTerrarium();
        if (!terrarium.getSettings().getBoolean(EarthWorldType.BOP_INTEGRATION)) {
            return;
        }

        Cover cover = event.getCover();
        TreeDecorator.Builder trees = event.getBuilder();

        if (cover.is(CoverSelectors.broadleafDeciduous())) {
            trees.addCandidate(BoPTrees.MAHOGANY);
            trees.addCandidate(BoPTrees.WILLOW);
        }

        if (cover.is(CoverSelectors.broadleafEvergreen())) {
            trees.addCandidate(BoPTrees.PALM);
            trees.addCandidate(BoPTrees.EUCALYPTUS);
            trees.addCandidate(BoPTrees.MANGROVE);
            trees.addCandidate(BoPTrees.EBONY);
        }

        if (cover.is(CoverSelectors.needleleafEvergreen())) {
            trees.addCandidate(BoPTrees.FIR);
        }
    }

    @SubscribeEvent
    public static void onConfigureFlowers(ConfigureFlowersEvent event) {
        // TODO: disabling to only have flower generation in compatibility mode
        //  otherwise the integration mode would have to be fully featured which it is not yet
        if (true) return;

        TerrariumWorld terrarium = event.getTerrarium();
        if (!terrarium.getSettings().getBoolean(EarthWorldType.BOP_INTEGRATION)) {
            return;
        }

        Cover cover = event.getCover();
        GrowthPredictors predictors = event.getPredictors();
        FlowerDecorator flowers = event.getFlowers();

        boolean hot = Climate.isHot(predictors.meanTemperature);
        boolean warm = Climate.isWarm(predictors.meanTemperature);
        boolean cold = Climate.isCold(predictors.meanTemperature);
        boolean frozen = Climate.isFrozen(predictors.minTemperature, predictors.meanTemperature);

        boolean wet = Climate.isWet(predictors.annualRainfall);

        if (cover.is(CoverMarkers.NEEDLELEAF) && cover.is(CoverMarkers.DECIDUOUS)) {
            flowers.add(BoPFlowers.LILY_OF_THE_VALLEY, 1.0F);
            flowers.add(BoPFlowers.CLOVER, 1.0F);
        }

        if (cover.is(CoverMarkers.PLAINS)) {
            flowers.add(BoPFlowers.CLOVER, 1.0F);
            flowers.add(BoPFlowers.LAVENDER, 0.1F);
            flowers.add(BoPFlowers.GOLDENROD, 0.1F);
        }

        if (cover.is(CoverMarkers.FLOODED)) {
            flowers.add(BoPFlowers.SWAMPFLOWER, 2.0F);
            flowers.add(BoPFlowers.BLUE_HYDRANGEA, 1.0F);
        }

        if (cover.is(CoverMarkers.CLOSED_FOREST)) {
            flowers.add(BoPFlowers.DEATHBLOOM, 0.1F);
        }

        if (cover.is(CoverMarkers.FOREST)) {
            flowers.add(BoPFlowers.GLOWFLOWER, 0.001F);
        }

        if (wet || cover.is(CoverMarkers.FLOODED)) {
            flowers.add(BoPFlowers.ORANGE_COSMOS, 1.0F);
            flowers.add(BoPFlowers.WHITE_ANEMONE, 1.0F);
            flowers.add(BoPFlowers.PINK_DAFFODIL, 1.0F);
            flowers.add(BoPFlowers.PINK_HIBISCUS, 1.0F);
        }

        if (hot) {
            flowers.add(BoPFlowers.WILDFLOWER, 2.0F);
        }

        if (warm && wet) {
            flowers.add(BoPFlowers.BROMELIAD, 3.0F);
        }

        if (cold) {
            flowers.add(BoPFlowers.VIOLET, 2.0F);
            flowers.add(BoPFlowers.BLUEBELLS, 2.0F);
        }

        if (frozen) {
            flowers.add(BoPFlowers.ICY_IRIS, 3.0F);
        }
    }

    @SubscribeEvent
    public static void onClassifyBiome(ClassifyBiomeEvent event) {
        TerrariumWorld terrarium = event.getTerrarium();
        if (!terrarium.getSettings().getBoolean(EarthWorldType.BOP_INTEGRATION)) {
            return;
        }

        GrowthPredictors predictors = event.getPredictors();

        if (!predictors.isFrozen()) {
            if (predictors.cover == Cover.LICHENS_AND_MOSSES) {
                event.setBiome(BOPBiomes.tundra.orNull());
                return;
            }

            if (predictors.cover == Cover.GRASSLAND) {
                event.setBiome(BOPBiomes.grassland.orNull());
                return;
            }

            if (predictors.cover.is(CoverMarkers.DENSE_SHRUBS) && Climate.isVeryDry(predictors.annualRainfall)) {
                if (predictors.isBarren() || SoilSelector.isDesertLike(predictors)) {
                    event.setBiome(BOPBiomes.brushland.orNull());
                } else {
                    event.setBiome(BOPBiomes.xeric_shrubland.orNull());
                }
                return;
            }

            if (predictors.isLand() && !predictors.cover.is(CoverMarkers.NO_VEGETATION)) {
                double mangrove = BoPTrees.Indicators.MANGROVE.evaluate(predictors);
                if (mangrove > 0.85) {
                    event.setBiome(BOPBiomes.mangrove.orNull());
                    return;
                }
            }

            if (predictors.slope >= 60 && !predictors.isCold() && predictors.cover.is(CoverMarkers.FOREST)) {
                event.setBiome(BOPBiomes.overgrown_cliffs.orNull());
                return;
            }

            if (predictors.isFlooded() && event.getBiome() == Biomes.SWAMPLAND) {
                double spruce = Trees.Indicators.SPRUCE.evaluate(predictors);
                if (spruce > 0.85) {
                    event.setBiome(BOPBiomes.wetland.orNull());
                    return;
                }
            }

            if (predictors.isForested() && event.getBiome() == Biomes.JUNGLE) {
                double jungle = Trees.Indicators.JUNGLE_LIKE.evaluate(predictors);
                double oak = Trees.Indicators.OAK.evaluate(predictors);
                double spruce = Trees.Indicators.SPRUCE.evaluate(predictors);
                double mahogany = BoPTrees.Indicators.MAHOGANY.evaluate(predictors);

                if (oak > jungle && oak > spruce && oak > mahogany) {
                    event.setBiome(BOPBiomes.rainforest.orNull());
                    return;
                } else if (spruce > jungle && spruce > oak && spruce > mahogany) {
                    event.setBiome(BOPBiomes.temperate_rainforest.orNull());
                    return;
                } else if (mahogany > jungle && mahogany > spruce && mahogany > oak) {
                    event.setBiome(BOPBiomes.tropical_rainforest.orNull());
                    return;
                }
            }
        }

        if (predictors.isForested()) {
            classifyForest(event, predictors);
            return;
        }
    }

    private static void classifyForest(ClassifyBiomeEvent event, GrowthPredictors predictors) {
        if (event.getBiome() == Biomes.FOREST && predictors.isFrozen()) {
            event.setBiome(BOPBiomes.snowy_forest.orNull());
            return;
        }

        if (!predictors.isFrozen()) {
            double eucalyptus = BoPTrees.Indicators.EUCALYPTUS.evaluate(predictors);
            if (eucalyptus > 0.85) {
                event.setBiome(BOPBiomes.eucalyptus_forest.orNull());
                return;
            }

            double birch = Trees.Indicators.BIRCH.evaluate(predictors);
            double spruce = Trees.Indicators.SPRUCE.evaluate(predictors);
            if (birch > 0.85 && spruce > 0.85) {
                event.setBiome(BOPBiomes.boreal_forest.orNull());
                return;
            }
        }

        double fir = BoPTrees.Indicators.FIR.evaluate(predictors);
        if (fir > 0.85) {
            if (predictors.isFrozen()) {
                event.setBiome(BOPBiomes.snowy_coniferous_forest.orNull());
            } else {
                event.setBiome(BOPBiomes.coniferous_forest.orNull());
            }
            return;
        }
    }

    @SubscribeEvent
    public static void onInitializeTerrariumGenerator(TerrariumInitializeGeneratorEvent event) {
        // TODO: disabling to only have ore generation in compatibility mode
        //  otherwise the integration mode would have to be fully featured which it is not yet
        if (true) return;

        if (event.getWorldType() != TerrariumEarth.GENERIC_WORLD_TYPE) return;

        World world = event.getWorld();
        GenerationSettings settings = event.getSettings();

        if (!settings.getBoolean(EarthWorldType.BOP_INTEGRATION)) {
            return;
        }

        if (settings.getBoolean(EarthWorldType.ORE_GENERATION)) {
            OreDecorationComposer oreComposer = new OreDecorationComposer(world);
            BoPOres.addTo(oreComposer);

            event.getGenerator().addDecorationComposer(oreComposer);
        }
    }
}
