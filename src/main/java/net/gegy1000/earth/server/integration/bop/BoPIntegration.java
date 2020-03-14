package net.gegy1000.earth.server.integration.bop;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.event.ConfigureTreesEvent;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.earth.server.world.composer.EarthTreeComposer;
import net.gegy1000.earth.server.world.composer.OreDecorationComposer;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverSelectors;
import net.gegy1000.terrarium.server.event.InitializeTerrariumWorldEvent;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class BoPIntegration {
    public static void setup() {
        MinecraftForge.TERRAIN_GEN_BUS.register(BoPIntegration.class);
    }

    @SubscribeEvent
    public static void onConfigureTrees(ConfigureTreesEvent event) {
        Cover cover = event.getCover();
        EarthTreeComposer.Builder trees = event.getBuilder();

        if (cover.is(CoverSelectors.broadleafDeciduous())) {
            trees.addCandidate(BoPTrees.MAHOGANY);
        }

        if (cover.is(CoverSelectors.broadleafEvergreen())) {
            trees.addCandidate(BoPTrees.PALM);
            trees.addCandidate(BoPTrees.EUCALYPTUS);
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
