package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.biome.BOPBiomes;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.event.ClassifyBiomeEvent;
import net.gegy1000.earth.server.event.ConfigureFlowersEvent;
import net.gegy1000.earth.server.event.ConfigureTreesEvent;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.earth.server.world.Rainfall;
import net.gegy1000.earth.server.world.Temperature;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.composer.OreDecorationComposer;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.cover.CoverSelectors;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.FlowerDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.gegy1000.terrarium.server.event.InitializeTerrariumWorldEvent;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
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
        Cover cover = event.getCover();
        TreeDecorator.Builder trees = event.getBuilder();

        if (cover.is(CoverSelectors.broadleafDeciduous())) {
            trees.addCandidate(BoPTrees.MAHOGANY);
        }

        if (cover.is(CoverSelectors.broadleafEvergreen())) {
            trees.addCandidate(BoPTrees.PALM);
            trees.addCandidate(BoPTrees.EUCALYPTUS);
        }
    }

    @SubscribeEvent
    public static void onConfigureFlowers(ConfigureFlowersEvent event) {
        Cover cover = event.getCover();
        GrowthPredictors predictors = event.getPredictors();
        FlowerDecorator flowers = event.getFlowers();

        boolean hot = Temperature.isHot(predictors.meanTemperature);
        boolean warm = Temperature.isWarm(predictors.meanTemperature);
        boolean cold = Temperature.isCold(predictors.meanTemperature);
        boolean frozen = Temperature.isFrozen(predictors.minTemperature, predictors.meanTemperature);

        boolean wet = Rainfall.isWet(predictors.annualRainfall);

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
        BiomeClassifier.Context ctx = event.getContext();

        if (ctx.isFrozen()) {
            return;
        }

        if (ctx.cover == Cover.LICHENS_AND_MOSSES) {
            event.setCanceled(true);
            event.setBiome(BOPBiomes.tundra.orNull());
        }

        if (ctx.cover == Cover.GRASSLAND) {
            event.setCanceled(true);
            event.setBiome(BOPBiomes.grassland.orNull());
        }
    }

    @SubscribeEvent
    public static void onInitializeTerrariumWorld(InitializeTerrariumWorldEvent event) {
        if (event.getWorldType() != TerrariumEarth.GENERIC_WORLD_TYPE) return;

        World world = event.getWorld();
        GenerationSettings settings = event.getSettings();

        if (settings.getBoolean(EarthWorldType.ORE_GENERATION)) {
            OreDecorationComposer oreComposer = new OreDecorationComposer(world);
            BoPOres.addTo(oreComposer);

            event.getGenerator().addDecorationComposer(oreComposer);
        }
    }
}
