package net.gegy1000.earth.server.integration.bop;

import net.gegy1000.earth.server.event.ConfigureCoverEvent;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.cover.decorator.CoverDecorators;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class BoPIntegration {
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(BoPIntegration.class);
    }

    @SubscribeEvent
    public static void onConfigureCover(ConfigureCoverEvent event) {
        CoverMarkers.FOREST.and(CoverMarkers.DECIDUOUS).and(CoverMarkers.BROADLEAF)
                .configureEach(c -> c.configureDecorator(CoverDecorators.TREES, trees -> {
                    trees.add(BoPTrees.MAHOGANY, 5.0F);
                    trees.add(BoPTrees.PALM, 2.0F);
                    trees.add(BoPTrees.EUCALYPTUS, 0.4F);
                }));

        CoverMarkers.FOREST.and(CoverMarkers.EVERGREEN).and(CoverMarkers.BROADLEAF)
                .configureEach(c -> c.configureDecorator(CoverDecorators.TREES, trees -> {
                    trees.add(BoPTrees.MAHOGANY, 1.0F);
                    trees.add(BoPTrees.PALM, 10.0F);
                    trees.add(BoPTrees.EUCALYPTUS, 2.0F);
                }));
    }
}
