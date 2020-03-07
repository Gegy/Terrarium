package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.event.ConfigureCoverEvent;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.carver.Carvers;
import net.gegy1000.earth.server.world.cover.decorator.CoverDecorators;
import net.gegy1000.earth.server.world.cover.decorator.VegetationDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.minecraftforge.common.MinecraftForge;

public final class CoverConfigurator {
    public static void configure() {
        CoverMarkers.FLOODED.configureEach(config -> {
            config.addCarver(Carvers.flooded(EarthDataKeys.TERRAIN_HEIGHT));
        });

        CoverConfigurator.configureForests();

        MinecraftForge.EVENT_BUS.post(new ConfigureCoverEvent());
    }

    private static void configureForests() {
        CoverMarkers.CLOSED_TO_OPEN_FOREST
                .configureEach(c -> c.addDecorator(CoverDecorators.TREES, new VegetationDecorator()
                        .withDensity(0.15F, 1.0F)
                        .withRadius(Trees.RADIUS)
                ));

        CoverMarkers.CLOSED_FOREST
                .configureEach(c -> c.addDecorator(CoverDecorators.TREES, new VegetationDecorator()
                        .withDensity(0.4F, 1.0F)
                        .withRadius(Trees.RADIUS)
                ));

        CoverMarkers.OPEN_FOREST
                .configureEach(c -> c.addDecorator(CoverDecorators.TREES, new VegetationDecorator()
                        .withDensity(0.15F, 0.4F)
                        .withRadius(Trees.RADIUS)
                ));

        CoverMarkers.FOREST.and(CoverMarkers.BROADLEAF).and(CoverMarkers.DECIDUOUS)
                .configureEach(c -> c.configureDecorator(CoverDecorators.TREES, trees -> {
                    trees.add(Trees.OAK, 10.0F);
                    trees.add(Trees.ACACIA, 10.0F);
                    trees.add(Trees.JUNGLE, 2.0F);
                }));

        CoverMarkers.FOREST.and(CoverMarkers.BROADLEAF).and(CoverMarkers.EVERGREEN)
                .configureEach(c -> c.configureDecorator(CoverDecorators.TREES, trees -> {
                    trees.add(Trees.OAK, 2.0F);
                    trees.add(Trees.ACACIA, 2.0F);
                    trees.add(Trees.JUNGLE, 10.0F);
                }));

        CoverMarkers.FOREST.and(CoverMarkers.NEEDLELEAF).and(CoverMarkers.DECIDUOUS)
                .configureEach(c -> c.configureDecorator(CoverDecorators.TREES, trees -> {
                    trees.add(Trees.BIRCH, 10.0F);
                    trees.add(Trees.ACACIA, 10.0F);
                    trees.add(Trees.SPRUCE, 2.0F);
                    trees.add(Trees.PINE, 2.0F);
                }));

        CoverMarkers.FOREST.and(CoverMarkers.NEEDLELEAF).and(CoverMarkers.EVERGREEN)
                .configureEach(c -> c.configureDecorator(CoverDecorators.TREES, trees -> {
                    trees.add(Trees.SPRUCE, 10.0F);
                    trees.add(Trees.PINE, 10.0F);
                    trees.add(Trees.BIRCH, 2.0F);
                    trees.add(Trees.ACACIA, 2.0F);
                }));
    }
}
