package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.event.ConfigureCoverEvent;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.carver.Carvers;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.minecraftforge.common.MinecraftForge;

public final class CoverConfigurator {
    public static void configure() {
        CoverMarkers.FLOODED.primeEach(primer -> {
            primer.addCarver(Carvers.flooded(EarthDataKeys.TERRAIN_HEIGHT));
        });

        CoverMarkers.FLOODED.decorateEach(decorator -> {
            decorator.requestGrassPerChunk(4);
        });

        Cover.GRASSLAND.or(Cover.LICHENS_AND_MOSSES).decorateEach(decorator -> {
            decorator.requestGrassPerChunk(4);
        });

        CoverMarkers.SHRUBS.decorateEach(decorator -> {
            decorator.requestGrassPerChunk(2);
        });

        CoverConfigurator.configureForests();

        MinecraftForge.EVENT_BUS.post(new ConfigureCoverEvent());
    }

    private static void configureForests() {
        CoverMarkers.FOREST.decorateEach(decorator -> {
            decorator.requestGrassPerChunk(2);
        });

        CoverMarkers.CLOSED_TO_OPEN_FOREST.decorateEach(decorator -> {
            decorator.setTreeDensity(0.15F, 1.0F);
        });

        CoverMarkers.CLOSED_FOREST.decorateEach(decorator -> {
            decorator.setTreeDensity(0.4F, 1.0F);
        });

        CoverMarkers.OPEN_FOREST.decorateEach(decorator -> {
            decorator.setTreeDensity(0.15F, 0.4F);
        });

        CoverMarkers.FOREST.and(CoverMarkers.BROADLEAF).and(CoverMarkers.DECIDUOUS)
                .decorateEach(decorator -> {
                    decorator.addCandidateTree(Trees.OAK);
                    decorator.addCandidateTree(Trees.ACACIA);
                });

        CoverMarkers.FOREST.and(CoverMarkers.BROADLEAF).and(CoverMarkers.EVERGREEN)
                .decorateEach(decorator -> {
                    decorator.addCandidateTree(Trees.JUNGLE);
                    decorator.addCandidateTree(Trees.BIG_JUNGLE);
                });

        CoverMarkers.FOREST.and(CoverMarkers.NEEDLELEAF).and(CoverMarkers.DECIDUOUS)
                .decorateEach(decorator -> {
                    decorator.addCandidateTree(Trees.BIRCH);
                    decorator.addCandidateTree(Trees.ACACIA);
                });

        CoverMarkers.FOREST.and(CoverMarkers.NEEDLELEAF).and(CoverMarkers.EVERGREEN)
                .decorateEach(decorator -> {
                    decorator.addCandidateTree(Trees.SPRUCE);
                    decorator.addCandidateTree(Trees.PINE);
                });
    }
}
